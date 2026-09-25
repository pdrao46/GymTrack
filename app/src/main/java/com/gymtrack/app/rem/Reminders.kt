package com.gymtrack.app.rem

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.gymtrack.app.GymTrackApp
import com.gymtrack.app.R
import com.gymtrack.app.data.DateUtils
import com.gymtrack.app.data.Graph
import com.gymtrack.app.data.Reminder
import com.gymtrack.app.data.SettingsState

object ReminderScheduler {

    fun rescheduleAll(context: Context) {
        if (!SettingsState.current.notificationsEnabled) return
        Graph.init(context)
        val list = try {
            Graph.db!!.let { RepoBridge.reminders() }
        } catch (e: Exception) {
            return
        }
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (r in list) {
            if (r.enabled) scheduleNext(context, r, am) else cancel(context, r, am)
        }
    }

    fun rescheduleOne(context: Context, r: Reminder) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (r.enabled && SettingsState.current.notificationsEnabled) scheduleNext(context, r, am)
        else cancel(context, r, am)
    }

    fun cancelAll(context: Context) {
        Graph.init(context)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            for (r in RepoBridge.reminders()) cancel(context, r, am)
        } catch (_: Exception) {}
    }

    private fun requestCode(r: Reminder, dayIso: Int) = (r.id * 10 + dayIso).toInt()

    private fun scheduleNext(context: Context, r: Reminder, am: AlarmManager) {
        val days = r.days.split(",").mapNotNull { it.trim().toIntOrNull() }
        if (days.isEmpty()) return
        val today = DateUtils.today()
        var bestDiff = Int.MAX_VALUE
        var bestDay = -1L
        for (i in 0..7) {
            val d = today + i
            val iso = DateUtils.dowIso(d)
            if (days.contains(iso)) {
                val minutesNow = nowMinutes()
                val target = r.hour * 60 + r.minute
                val diff = if (i == 0) {
                    if (target > minutesNow) target - minutesNow else Int.MAX_VALUE
                } else i * 24 * 60 + (target - minutesNow)
                if (diff < bestDiff) {
                    bestDiff = diff
                    bestDay = d
                }
            }
        }
        if (bestDay < 0) return
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = DateUtils.startOfDayMillis(bestDay)
        cal.set(java.util.Calendar.HOUR_OF_DAY, r.hour)
        cal.set(java.util.Calendar.MINUTE, r.minute)
        cal.set(java.util.Calendar.SECOND, 0)
        val pi = pending(context, r, DateUtils.dowIso(bestDay))
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }

    private fun cancel(context: Context, r: Reminder, am: AlarmManager) {
        for (iso in 1..7) am.cancel(pending(context, r, iso))
    }

    private fun pending(context: Context, r: Reminder, dayIso: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        intent.putExtra("reminderId", r.id)
        return PendingIntent.getBroadcast(
            context, requestCode(r, dayIso), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun nowMinutes(): Int {
        val cal = java.util.Calendar.getInstance()
        return cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
    }
}

/** Acesso seguro ao repositório a partir de receivers */
object RepoBridge {
    fun reminders(): List<Reminder> = com.gymtrack.app.data.Repo.reminders()
    fun reminder(id: Long): Reminder? = com.gymtrack.app.data.Repo.reminder(id)
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("reminderId", -1)
        val pending = goAsync()
        Thread {
            try {
                Graph.init(context)
                val r = RepoBridge.reminder(id)
                if (r != null && r.enabled && SettingsState.current.notificationsEnabled) {
                    notify(context, r.title.ifBlank { "Hora de treinar! 💪" },
                        "Seu treino está esperando. Abra o GymTrack para começar.")
                    ReminderScheduler.rescheduleOne(context, r)
                }
            } catch (_: Exception) {
            } finally {
                pending.finish()
            }
        }.start()
    }

    private fun notify(context: Context, title: String, text: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pi = PendingIntent.getActivity(
            context, 1001, Intent(context, com.gymtrack.app.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, GymTrackApp.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_stat_dumbbell)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        nm.notify(idFor(title), n)
    }

    private fun idFor(s: String): Int = abs(s.hashCode())
    private fun abs(v: Int): Int = if (v == Int.MIN_VALUE) 0 else kotlin.math.abs(v)
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pending = goAsync()
            Thread {
                try {
                    ReminderScheduler.rescheduleAll(context)
                } catch (_: Exception) {
                } finally {
                    pending.finish()
                }
            }.start()
        }
    }
}

/** Sinal sonoro/vibração quando o descanso termina */
object RestSignal {
    fun play(context: Context) {
        val s = SettingsState.current
        if (s.soundEnabled) {
            try {
                val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
            } catch (_: Exception) {}
        }
        if (s.vibrateEnabled) {
            try {
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 250, 150, 250), -1))
            } catch (_: Exception) {}
        }
        if (s.notificationsEnabled) {
            try {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val n = NotificationCompat.Builder(context, GymTrackApp.CHANNEL_WORKOUT)
                    .setSmallIcon(R.drawable.ic_stat_dumbbell)
                    .setContentTitle("Descanso encerrado ⏱️")
                    .setContentText("Próxima série! Bora lá. 💪")
                    .setAutoCancel(true)
                    .build()
                nm.notify(99901, n)
            } catch (_: Exception) {}
        }
    }
}
