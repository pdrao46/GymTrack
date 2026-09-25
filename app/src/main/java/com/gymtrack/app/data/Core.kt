package com.gymtrack.app.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------- Modelo de dados ----------

data class Workout(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val dayOfWeek: Int? = null, // 1=Segunda .. 7=Domingo
    val time: String = "",
    val estimatedMinutes: Int = 0,
    val notes: String = "",
    val colorIndex: Int = 0,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class Exercise(
    val id: Long = 0,
    val name: String = "",
    val muscleGroup: String = "",
    val secondary: String = "",
    val equipment: String = "",
    val instructions: String = "",
    val tips: String = "",
    val mistakes: String = "",
    val personalNote: String = "",
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false
)

data class WorkoutExercise(
    val id: Long = 0,
    val workoutId: Long = 0,
    val exerciseId: Long = 0,
    val exerciseName: String = "",
    val muscleGroup: String = "",
    val orderIndex: Int = 0,
    val sets: Int = 3,
    val repsMin: Int = 8,
    val repsMax: Int = 12,
    val weight: Double = 0.0,
    val restSeconds: Int = 90,
    val rir: String = "",
    val rpe: String = "",
    val tempo: String = "",
    val method: String = "",
    val notes: String = ""
)

data class Session(
    val id: Long = 0,
    val workoutId: Long? = null,
    val workoutName: String = "",
    val dateEpochDay: Long = 0,
    val startMillis: Long = 0,
    val endMillis: Long? = null,
    val pausedMillis: Long = 0,
    val pausedAt: Long? = null,
    val completed: Boolean = false,
    val feeling: Int = 0,      // 1..5
    val energy: Int = 0,       // 1..5
    val motivation: Int = 0,   // 1..5
    val difficulty: Int = 0,   // 1..5
    val notes: String = ""
)

data class SetLog(
    val id: Long = 0,
    val sessionId: Long = 0,
    val exerciseId: Long = 0,
    val exerciseName: String = "",
    val muscleGroup: String = "",
    val setNumber: Int = 1,
    val reps: Int = 0,
    val weight: Double = 0.0,
    val rir: String = "",
    val rpe: String = "",
    val completed: Boolean = true,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Goal(
    val id: Long = 0,
    val title: String = "",
    val type: String = GoalType.WEEKLY,
    val target: Double = 4.0,
    val exerciseName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val done: Boolean = false
)

object GoalType {
    const val WEEKLY = "semanal"      // treinar X vezes por semana
    const val TOTAL = "total"         // completar X treinos
    const val STREAK = "streak"       // X dias seguidos
    const val LOAD = "carga"          // aumentar carga de um exercício
    const val VOLUME = "volume"       // volume semanal (kg)
}

data class Measurement(
    val id: Long = 0,
    val dateEpochDay: Long = 0,
    val weight: Double = -1.0,
    val arm: Double = -1.0,
    val chest: Double = -1.0,
    val waist: Double = -1.0,
    val hip: Double = -1.0,
    val thigh: Double = -1.0,
    val calf: Double = -1.0,
    val note: String = ""
)

data class Reminder(
    val id: Long = 0,
    val title: String = "",
    val type: String = "treino", // treino | medidas | personalizado
    val hour: Int = 7,
    val minute: Int = 30,
    val days: String = "1,2,3,4,5,6,7", // ISO: 1=Seg..7=Dom
    val enabled: Boolean = true
)

data class Note(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val isFavorite: Boolean = false
)

data class Tip(
    val id: Long = 0,
    val category: String = "",
    val title: String = "",
    val content: String = "",
    val isFavorite: Boolean = false
)

data class ChecklistItem(
    val id: Long = 0,
    val category: String = "pre", // pre | post
    val label: String = "",
    val checked: Boolean = false,
    val orderIndex: Int = 0
)

data class ProgressPhoto(
    val id: Long = 0,
    val dateEpochDay: Long = 0,
    val path: String = "",
    val note: String = ""
)

data class PersonalRecord(
    val exerciseName: String,
    val muscleGroup: String,
    val maxWeight: Double,
    val reps: Int,
    val dateEpochDay: Long
)

// ---------- Infraestrutura ----------

object RefreshBus {
    val tick = MutableStateFlow(0)
    fun bump() { tick.value = tick.value + 1 }
}

object Graph {
    @Volatile var db: GymDb? = null
        private set
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(context: Context) {
        if (db == null) {
            synchronized(this) {
                if (db == null) db = GymDb(context.applicationContext)
            }
        }
    }

    fun <T> launch(block: suspend () -> T) {
        scope.launch { block() }
    }
}

// ---------- Utilidades de data ----------

object DateUtils {
    val PT = Locale.forLanguageTag("pt-BR")
    private val fmtDdMm = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT)
    private val fmtLong = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PT)
    private val fmtMonth = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", PT)

    fun today(): Long = LocalDate.now().toEpochDay()

    /** Converte um timestamp em epochDay local */
    fun epochDayOfMillis(millis: Long): Long =
        java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay()

    /** Milissegundos do início (00:00) do dia informado */
    fun startOfDayMillis(epochDay: Long): Long =
        LocalDate.ofEpochDay(epochDay).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun dowIso(epochDay: Long): Int = LocalDate.ofEpochDay(epochDay).dayOfWeek.value // 1..7

    fun addDays(epochDay: Long, n: Int): Long = LocalDate.ofEpochDay(epochDay).plusDays(n.toLong()).toEpochDay()

    fun fmtDate(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(fmtDdMm)

    fun fmtLong(epochDay: Long): String {
        val s = LocalDate.ofEpochDay(epochDay).format(fmtLong)
        return s.replaceFirstChar { it.uppercase(PT) }
    }

    fun fmtMonth(year: Int, month: Int): String {
        val d = LocalDate.of(year, month, 1)
        return d.format(fmtMonth).replaceFirstChar { it.uppercase(PT) }
    }

    fun dowShort(iso: Int): String = when (iso) {
        1 -> "Seg"; 2 -> "Ter"; 3 -> "Qua"; 4 -> "Qui"; 5 -> "Sex"; 6 -> "Sáb"; else -> "Dom"
    }

    fun dowLong(iso: Int): String = when (iso) {
        1 -> "Segunda"; 2 -> "Terça"; 3 -> "Quarta"; 4 -> "Quinta"; 5 -> "Sexta"; 6 -> "Sábado"; else -> "Domingo"
    }

    /** Início (segunda) da semana que contém o dia informado */
    fun weekStart(epochDay: Long): Long {
        val dow = dowIso(epochDay)
        return epochDay - (dow - 1)
    }

    fun fmtDuration(minutes: Long): String {
        if (minutes <= 0) return "0min"
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h${m.toString().padStart(2, '0')}min" else "${m}min"
    }

    fun fmtClock(totalSeconds: Long): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return m.toString().padStart(2, '0') + ":" + s.toString().padStart(2, '0')
    }

    fun fmtTimeMillis(millis: Long): String = fmtClock(millis / 1000)

    /** "07:30" -> minutos desde meia-noite; null se inválido */
    fun parseTime(s: String): Pair<Int, Int>? {
        val parts = s.split(":")
        if (parts.size != 2) return null
        val h = parts[0].trim().toIntOrNull() ?: return null
        val m = parts[1].trim().toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h to m
    }
}

// ---------- Formatação de peso (unidade configurável) ----------

object Disp {
    const val LB_PER_KG = 2.20462262

    fun fmtKg(kg: Double): String =
        if (kg == kg.toLong().toDouble()) kg.toLong().toString() else String.format(PT, "%.1f", kg)

    /** Converte kg (armazenado) para a unidade escolhida e formata */
    fun weight(kg: Double, unit: String, withUnit: Boolean = true): String {
        if (kg <= 0.0) return if (withUnit) "—" else "0"
        val v = if (unit == "lb") kg * LB_PER_KG else kg
        val s = fmtKg(v)
        return if (withUnit) "$s $unit" else s
    }

    /** Converte o valor digitado (na unidade escolhida) para kg */
    fun toKg(value: Double, unit: String): Double =
        if (unit == "lb") value / LB_PER_KG else value

    fun fromKg(kg: Double, unit: String): Double =
        if (unit == "lb") kg * LB_PER_KG else kg
}
