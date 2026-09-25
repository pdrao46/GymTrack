package com.gymtrack.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.gymtrack.app.data.Graph
import com.gymtrack.app.data.SettingsRepo
import com.gymtrack.app.rem.ReminderScheduler

class GymTrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.init(this)
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDERS, "Lembretes de treino", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Lembretes de horário de treino e medidas"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_WORKOUT, "Treino e descanso", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos durante o treino e fim do descanso"
            }
        )
        SettingsRepo.startCollecting(this)
        ReminderScheduler.rescheduleAll(this)
    }

    companion object {
        const val CHANNEL_REMINDERS = "lembretes"
        const val CHANNEL_WORKOUT = "treino"
    }
}
