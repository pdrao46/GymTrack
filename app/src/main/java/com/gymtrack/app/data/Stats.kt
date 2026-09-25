package com.gymtrack.app.data

import kotlin.math.abs
import kotlin.math.roundToInt

data class WeekStats(
    val planned: Int,
    val completed: Int,
    val exercisesDone: Int,
    val volume: Double,
    val frequency: Int // porcentagem 0..100
)

data class Totals(
    val sessions: Int,
    val totalMinutes: Long,
    val avgMinutes: Long,
    val exercises: Int,
    val sets: Int,
    val reps: Int,
    val volume: Double,
    val weeklyFrequency: Int,
    val bestSession: String,
    val maxLoad: String
)

object Stats {

    fun volumeOf(logs: List<SetLog>): Double =
        logs.filter { it.completed && it.weight > 0 }.sumOf { it.reps * it.weight }

    fun sessionsOnDay(all: List<Session>, day: Long): List<Session> = all.filter { it.dateEpochDay == day }

    fun completedDaysIn(all: List<Session>, fromDay: Long, toDay: Long): Set<Long> =
        all.filter { it.dateEpochDay in fromDay..toDay }.map { it.dateEpochDay }.toSet()

    /** Estatísticas da semana (seg-dom) que contém hoje */
    fun currentWeek(workouts: List<Workout>, sessions: List<Session>, logs: List<SetLog>): WeekStats {
        val today = DateUtils.today()
        val start = DateUtils.weekStart(today)
        val end = start + 6
        val weekSessions = sessions.filter { it.dateEpochDay in start..end }
        val completedDays = weekSessions.map { it.dateEpochDay }.toSet()
        val planned = workouts.count { it.dayOfWeek != null }
        val weekLogs = logs.filter { it.timestamp >= startMillisOf(start) && it.timestamp < startMillisOf(end + 1) && it.completed }
        val exercisesDone = weekLogs.map { it.exerciseName }.distinct().size
        val volume = volumeOf(weekLogs)
        val freq = if (planned == 0) 0 else ((completedDays.size.toDouble() / planned) * 100).roundToInt()
        return WeekStats(planned, completedDays.size, exercisesDone, volume, freq.coerceAtMost(100))
    }

    fun startMillisOf(epochDay: Long): Long =
        epochDay * 86_400_000L

    /** Sequência de dias treinados (contando hoje ou ontem como âncora) */
    fun streak(sessions: List<Session>): Int {
        val days = sessions.map { it.dateEpochDay }.toHashSet()
        var day = DateUtils.today()
        if (!days.contains(day)) {
            day -= 1
            if (!days.contains(day)) return 0
        }
        var count = 0
        while (days.contains(day)) {
            count++
            day -= 1
        }
        return count
    }

    fun totals(sessions: List<Session>, logs: List<SetLog>, maxWeightDisplay: String, bestDisplay: String): Totals {
        val done = sessions.filter { s -> s.endMillis != null }
        val doneIds = done.map { it.id }.toSet()
        val logsDone = logs.filter { it.completed && doneIds.contains(it.sessionId) }
        val totalMinutes = done.sumOf { durationOf(it) / 60000L }
        val avg = if (done.isEmpty()) 0L else totalMinutes / done.size
        val volume = volumeOf(logsDone)
        val last4WeeksStart = DateUtils.weekStart(DateUtils.today()) - 21
        val w4 = done.count { it.dateEpochDay >= last4WeeksStart }
        val weeklyFreq = (w4 / 4.0).roundToInt()
        val maxLog = logsDone.maxByOrNull { it.weight }
        val bestSession = done.maxByOrNull { volumeOf(logsDone.filter { l -> l.sessionId == it.id }) }
        return Totals(
            sessions = done.size,
            totalMinutes = totalMinutes,
            avgMinutes = avg,
            exercises = logsDone.map { it.exerciseName }.distinct().size,
            sets = logsDone.size,
            reps = logsDone.sumOf { it.reps },
            volume = volume,
            weeklyFrequency = weeklyFreq,
            bestSession = bestDisplay,
            maxLoad = maxWeightDisplay
        )
    }

    fun durationOf(s: Session): Long {
        val end = s.endMillis ?: return 0
        val paused = s.pausedMillis + if (s.pausedAt != null) (System.currentTimeMillis() - s.pausedAt) else 0
        return ((end - s.startMillis - paused).coerceAtLeast(0))
    }

    fun elapsedMillis(s: Session): Long {
        val paused = s.pausedMillis + if (s.pausedAt != null) (System.currentTimeMillis() - s.pausedAt) else 0
        return (System.currentTimeMillis() - s.startMillis - paused).coerceAtLeast(0)
    }

    /** Volume por semana (últimas N semanas, mais antiga primeiro) */
    fun weeklyVolume(logs: List<SetLog>, weeks: Int): List<Pair<String, Double>> {
        val thisWeekStart = DateUtils.weekStart(DateUtils.today())
        val out = ArrayList<Pair<String, Double>>()
        for (i in weeks - 1 downTo 0) {
            val start = thisWeekStart - i * 7L
            val end = start + 6
            val v = logs.filter { it.completed && it.timestamp >= startMillisOf(start) && it.timestamp < startMillisOf(end + 1) }
                .sumOf { it.reps * it.weight }
            val label = if (i == 0) "Atual" else "-$i"
            out.add(label to v)
        }
        return out
    }

    /** Treinos concluídos por semana (últimas N semanas) */
    fun weeklySessions(sessions: List<Session>, weeks: Int): List<Pair<String, Int>> {
        val thisWeekStart = DateUtils.weekStart(DateUtils.today())
        val out = ArrayList<Pair<String, Int>>()
        for (i in weeks - 1 downTo 0) {
            val start = thisWeekStart - i * 7L
            val end = start + 6
            val c = sessions.filter { it.dateEpochDay in start..end }.map { it.dateEpochDay }.distinct().size
            val label = if (i == 0) "Atual" else "-$i"
            out.add(label to c)
        }
        return out
    }

    /** Recordes pessoais por exercício */
    fun personalRecords(logs: List<SetLog>): List<PersonalRecord> {
        val byExercise = HashMap<String, PersonalRecord>()
        for (l in logs) {
            if (!l.completed || l.weight <= 0) continue
            val cur = byExercise[l.exerciseName]
            if (cur == null || l.weight > cur.maxWeight || (l.weight == cur.maxWeight && l.reps > cur.reps)) {
                byExercise[l.exerciseName] = PersonalRecord(l.exerciseName, l.muscleGroup, l.weight, l.reps, DateUtils.epochDayOfMillis(l.timestamp))
            }
        }
        return byExercise.values.sortedByDescending { it.maxWeight }
    }

    /** Dias treinados num mês */
    fun trainedDaysInMonth(sessions: List<Session>, year: Int, month: Int): Set<Long> {
        val first = java.time.LocalDate.of(year, month, 1).toEpochDay()
        val last = java.time.LocalDate.of(year, month, firstOfMonthLength(year, month)).toEpochDay()
        return sessions.filter { it.dateEpochDay in first..last }.map { it.dateEpochDay }.toSet()
    }

    private fun firstOfMonthLength(year: Int, month: Int): Int =
        java.time.YearMonth.of(year, month).lengthOfMonth()

    /** Status de cada dia do mês para o calendário de frequência */
    fun monthDayStatus(
        year: Int, month: Int,
        sessions: List<Session>, workouts: List<Workout>
    ): List<Pair<Long, Char>> {
        val first = java.time.LocalDate.of(year, month, 1)
        val len = first.lengthOfMonth()
        val trained = trainedDaysInMonth(sessions, year, month)
        val today = DateUtils.today()
        val plannedDows = workouts.mapNotNull { it.dayOfWeek }.toSet()
        val out = ArrayList<Pair<Long, Char>>()
        for (i in 0 until len) {
            val day = first.plusDays(i.toLong()).toEpochDay()
            val dow = DateUtils.dowIso(day)
            val status = when {
                trained.contains(day) -> 'T' // treinado
                day < today && plannedDows.contains(dow) -> 'F' // faltou (planejado e não treinado)
                plannedDows.contains(dow) -> 'P' // planejado (futuro/hoje)
                else -> 'D' // descanso
            }
            out.add(day to status)
        }
        return out
    }

    /** Progresso de uma meta: (atual, texto) */
    fun goalProgress(
        goal: Goal, sessions: List<Session>, logs: List<SetLog>
    ): Pair<Double, String> {
        return when (goal.type) {
            GoalType.WEEKLY -> {
                val ws = currentWeek(emptyList(), sessions, logs)
                val cur = ws.completed.toDouble()
                cur to "${cur.toInt()} de ${goal.target.toInt()} esta semana"
            }
            GoalType.TOTAL -> {
                val cur = sessions.filter { it.endMillis != null }.size.toDouble()
                cur to "${cur.toInt()} de ${goal.target.toInt()} treinos"
            }
            GoalType.STREAK -> {
                val cur = streak(sessions).toDouble()
                cur to "${cur.toInt()} de ${goal.target.toInt()} dias seguidos"
            }
            GoalType.VOLUME -> {
                val ws = currentWeek(emptyList(), sessions, logs)
                val cur = ws.volume
                cur to "${Disp.fmtKg(cur)} kg de ${Disp.fmtKg(goal.target)} kg nesta semana"
            }
            GoalType.LOAD -> {
                val hist = logs.filter { it.completed && it.exerciseName == goal.exerciseName && it.weight > 0 }
                val cur = hist.maxByOrNull { it.timestamp }?.weight ?: 0.0
                cur to if (cur > 0) "Carga atual: ${Disp.fmtKg(cur)} kg (meta: ${Disp.fmtKg(goal.target)} kg)" else "Sem registros ainda"
            }
            else -> 0.0 to ""
        }
    }

    /** Insights inteligentes baseados apenas nos dados registrados */
    fun insights(workouts: List<Workout>, sessions: List<Session>, logs: List<SetLog>): List<String> {
        val out = ArrayList<String>()
        if (sessions.isEmpty()) {
            out.add("Registre seu primeiro treino para o GymTrack começar a gerar informações. 💪")
            return out
        }
        // frequência da semana
        val ws = currentWeek(workouts, sessions, logs)
        if (ws.planned > 0) {
            out.add("Você concluiu ${ws.completed} de ${ws.planned} treinos planejados nesta semana.")
        }
        // volume semana x semana
        val wv = weeklyVolume(logs, 2)
        if (wv.size == 2) {
            val prev = wv[0].second
            val cur = wv[1].second
            if (prev > 0) {
                val diff = ((cur - prev) / prev * 100).roundToInt()
                if (diff > 5) out.add("Seu volume desta semana está ${diff}% maior que a semana anterior. 📈")
                else if (diff < -5) out.add("Seu volume desta semana está ${abs(diff)}% menor que a semana anterior. 📉")
                else out.add("Seu volume está estável em relação à semana anterior.")
            }
        }
        // carga estagnada
        val byEx = HashMap<String, MutableList<Double>>()
        val recent = logs.filter { it.completed && it.weight > 0 }.sortedByDescending { it.timestamp }.take(300)
        for (l in recent) {
            val list = byEx.getOrPut(l.exerciseName) { ArrayList() }
            if (list.size < 3) list.add(l.weight)
        }
        for ((name, ws3) in byEx) {
            if (ws3.size == 3 && abs(ws3[0] - ws3[1]) < 0.01 && abs(ws3[1] - ws3[2]) < 0.01) {
                out.add("Você usou a mesma carga (${Disp.fmtKg(ws3[0])} kg) nos últimos 3 registros de \"$name\". Hora de progredir! 🔝")
                break
            }
        }
        // streak
        val st = streak(sessions)
        if (st >= 3) out.add("🔥 Sequência de $st dias treinando. Não quebre a corrente!")
        // recorde recente
        val weekAgo = System.currentTimeMillis() - 7L * 24 * 3600 * 1000
        val prThisWeek = logs.filter { it.completed && it.timestamp >= weekAgo }
        if (prThisWeek.isNotEmpty()) {
            val best = prThisWeek.maxByOrNull { it.weight }
            if (best != null && best.weight > 0) {
                out.add("Maior carga dos últimos 7 dias: ${Disp.fmtKg(best.weight)} kg no \"${best.exerciseName}\". 🏆")
            }
        }
        return out.take(5)
    }
}
