package com.gymtrack.app.data

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.gymtrack.app.data.RefreshBus

// ---------- Helpers de cursor ----------

private fun Cursor.s(col: String): String {
    val i = getColumnIndex(col)
    return if (i < 0 || isNull(i)) "" else getString(i)
}
private fun Cursor.lng(col: String): Long {
    val i = getColumnIndex(col)
    return if (i < 0 || isNull(i)) 0L else getLong(i)
}
private fun Cursor.lngNull(col: String): Long? {
    val i = getColumnIndex(col)
    return if (i < 0 || isNull(i)) null else getLong(i)
}
private fun Cursor.int(col: String): Int = lng(col).toInt()
private fun Cursor.intNull(col: String): Int? = lngNull(col)?.toInt()
private fun Cursor.dbl(col: String): Double {
    val i = getColumnIndex(col)
    return if (i < 0 || isNull(i)) 0.0 else getDouble(i)
}
private fun Cursor.bool(col: String): Boolean = lng(col) == 1L

private fun <T> Cursor.mapList(f: (Cursor) -> T): List<T> = use {
    val out = ArrayList<T>()
    while (moveToNext()) out.add(f(this))
    out
}

// ---------- Repositório ----------

/** Acesso curto ao banco (inicializado por Graph.init) */
private val db: SQLiteDatabase get() = Graph.db!!.writableDatabase

object Repo {

    // ===== Treinos =====

    fun workouts(): List<Workout> {
        return db!!.rawQuery(
            "SELECT * FROM workouts ORDER BY CASE WHEN dayOfWeek IS NULL THEN 8 ELSE dayOfWeek END, createdAt DESC", null
        ).mapList { c ->
            Workout(
                id = c.lng("id"), name = c.s("name"), description = c.s("description"),
                dayOfWeek = c.intNull("dayOfWeek"), time = c.s("time"),
                estimatedMinutes = c.int("estimatedMinutes"), notes = c.s("notes"),
                colorIndex = c.int("colorIndex"), isFavorite = c.bool("isFavorite"),
                createdAt = c.lng("createdAt")
            )
        }
    }

    fun workout(id: Long): Workout? {
        return db!!.rawQuery("SELECT * FROM workouts WHERE id = ?", arrayOf(id.toString())).use { c ->
            if (c.moveToFirst()) Workout(
                id = c.lng("id"), name = c.s("name"), description = c.s("description"),
                dayOfWeek = c.intNull("dayOfWeek"), time = c.s("time"),
                estimatedMinutes = c.int("estimatedMinutes"), notes = c.s("notes"),
                colorIndex = c.int("colorIndex"), isFavorite = c.bool("isFavorite"),
                createdAt = c.lng("createdAt")
            ) else null
        }
    }

    fun insertWorkout(w: Workout): Long {
        val id = db!!.insert("workouts", null, GymDb.cv(
            "name" to w.name, "description" to w.description, "dayOfWeek" to w.dayOfWeek,
            "time" to w.time, "estimatedMinutes" to w.estimatedMinutes, "notes" to w.notes,
            "colorIndex" to w.colorIndex, "isFavorite" to w.isFavorite, "createdAt" to w.createdAt
        ))
        RefreshBus.bump()
        return id
    }

    fun updateWorkout(w: Workout) {
        db!!.update("workouts", GymDb.cv(
            "name" to w.name, "description" to w.description, "dayOfWeek" to w.dayOfWeek,
            "time" to w.time, "estimatedMinutes" to w.estimatedMinutes, "notes" to w.notes,
            "colorIndex" to w.colorIndex, "isFavorite" to w.isFavorite
        ), "id = ?", arrayOf(w.id.toString()))
        RefreshBus.bump()
    }

    fun deleteWorkout(id: Long) {
        db!!.delete("workout_exercises", "workoutId = ?", arrayOf(id.toString()))
        db!!.delete("workouts", "id = ?", arrayOf(id.toString()))
        RefreshBus.bump()
    }

    fun duplicateWorkout(id: Long): Long {
        val w = workout(id) ?: return -1
        val nid = insertWorkout(
            w.copy(id = 0, name = w.name + " (cópia)", dayOfWeek = null, time = "", isFavorite = false,
                createdAt = System.currentTimeMillis())
        )
        for (we in workoutExercises(id)) {
            insertWorkoutExercise(we.copy(id = 0, workoutId = nid))
        }
        RefreshBus.bump()
        return nid
    }

    // ===== Exercícios do treino =====

    fun workoutExercises(workoutId: Long): List<WorkoutExercise> {
        return db!!.rawQuery(
            "SELECT * FROM workout_exercises WHERE workoutId = ? ORDER BY orderIndex", arrayOf(workoutId.toString())
        ).mapList { c ->
            WorkoutExercise(
                id = c.lng("id"), workoutId = c.lng("workoutId"), exerciseId = c.lng("exerciseId"),
                exerciseName = c.s("exerciseName"), muscleGroup = c.s("muscleGroup"),
                orderIndex = c.int("orderIndex"), sets = c.int("sets"),
                repsMin = c.int("repsMin"), repsMax = c.int("repsMax"), weight = c.dbl("weight"),
                restSeconds = c.int("restSeconds"), rir = c.s("rir"), rpe = c.s("rpe"),
                tempo = c.s("tempo"), method = c.s("method"), notes = c.s("notes")
            )
        }
    }

    fun insertWorkoutExercise(we: WorkoutExercise): Long {
        val id = db!!.insert("workout_exercises", null, GymDb.cv(
            "workoutId" to we.workoutId, "exerciseId" to we.exerciseId, "exerciseName" to we.exerciseName,
            "muscleGroup" to we.muscleGroup, "orderIndex" to we.orderIndex, "sets" to we.sets,
            "repsMin" to we.repsMin, "repsMax" to we.repsMax, "weight" to we.weight,
            "restSeconds" to we.restSeconds, "rir" to we.rir, "rpe" to we.rpe,
            "tempo" to we.tempo, "method" to we.method, "notes" to we.notes
        ))
        return id
    }

    fun deleteWorkoutExercisesFor(workoutId: Long) {
        db!!.delete("workout_exercises", "workoutId = ?", arrayOf(workoutId.toString()))
    }

    /** Substitui todos os exercícios de um treino (usado na edição) */
    fun saveWorkoutExercises(workoutId: Long, list: List<WorkoutExercise>) {
        val db = db!!
        db.beginTransaction()
        try {
            db.delete("workout_exercises", "workoutId = ?", arrayOf(workoutId.toString()))
            list.forEachIndexed { i, we -> insertWorkoutExercise(we.copy(workoutId = workoutId, orderIndex = i)) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        RefreshBus.bump()
    }

    // ===== Banco de exercícios =====

    fun exercises(): List<Exercise> {
        return db!!.rawQuery("SELECT * FROM exercises ORDER BY isCustom DESC, name", null).mapList { c ->
            Exercise(
                id = c.lng("id"), name = c.s("name"), muscleGroup = c.s("muscleGroup"),
                secondary = c.s("secondary"), equipment = c.s("equipment"),
                instructions = c.s("instructions"), tips = c.s("tips"), mistakes = c.s("mistakes"),
                personalNote = c.s("personalNote"), isCustom = c.bool("isCustom"), isFavorite = c.bool("isFavorite")
            )
        }
    }

    fun exercise(id: Long): Exercise? {
        return db!!.rawQuery("SELECT * FROM exercises WHERE id = ?", arrayOf(id.toString())).use { c ->
            if (c.moveToFirst()) Exercise(
                id = c.lng("id"), name = c.s("name"), muscleGroup = c.s("muscleGroup"),
                secondary = c.s("secondary"), equipment = c.s("equipment"),
                instructions = c.s("instructions"), tips = c.s("tips"), mistakes = c.s("mistakes"),
                personalNote = c.s("personalNote"), isCustom = c.bool("isCustom"), isFavorite = c.bool("isFavorite")
            ) else null
        }
    }

    fun insertExercise(e: Exercise): Long {
        val id = db!!.insert("exercises", null, GymDb.cv(
            "name" to e.name, "muscleGroup" to e.muscleGroup, "secondary" to e.secondary,
            "equipment" to e.equipment, "instructions" to e.instructions, "tips" to e.tips,
            "mistakes" to e.mistakes, "personalNote" to e.personalNote,
            "isCustom" to e.isCustom, "isFavorite" to e.isFavorite
        ))
        RefreshBus.bump()
        return id
    }

    fun updateExercise(e: Exercise) {
        db!!.update("exercises", GymDb.cv(
            "name" to e.name, "muscleGroup" to e.muscleGroup, "secondary" to e.secondary,
            "equipment" to e.equipment, "instructions" to e.instructions, "tips" to e.tips,
            "mistakes" to e.mistakes, "personalNote" to e.personalNote, "isFavorite" to e.isFavorite
        ), "id = ?", arrayOf(e.id.toString()))
        // mantém o nome em sincronia nos treinos e nos registros
        db!!.update("workout_exercises", GymDb.cv("exerciseName" to e.name, "muscleGroup" to e.muscleGroup),
            "exerciseId = ?", arrayOf(e.id.toString()))
        db!!.update("set_logs", GymDb.cv("exerciseName" to e.name, "muscleGroup" to e.muscleGroup),
            "exerciseId = ?", arrayOf(e.id.toString()))
        RefreshBus.bump()
    }

    fun deleteExercise(id: Long) {
        db!!.delete("exercises", "id = ? AND isCustom = 1", arrayOf(id.toString()))
        RefreshBus.bump()
    }

    // ===== Sessões =====

    fun sessions(limit: Int = 200): List<Session> {
        return db!!.rawQuery(
            "SELECT * FROM sessions ORDER BY startMillis DESC LIMIT " + limit, null
        ).mapList { c ->
            Session(
                id = c.lng("id"), workoutId = c.lngNull("workoutId"), workoutName = c.s("workoutName"),
                dateEpochDay = c.lng("dateEpochDay"), startMillis = c.lng("startMillis"),
                endMillis = c.lngNull("endMillis"), pausedMillis = c.lng("pausedMillis"),
                pausedAt = c.lngNull("pausedAt"), completed = c.bool("completed"),
                feeling = c.int("feeling"), energy = c.int("energy"), motivation = c.int("motivation"),
                difficulty = c.int("difficulty"), notes = c.s("notes")
            )
        }
    }

    fun session(id: Long): Session? {
        return db!!.rawQuery("SELECT * FROM sessions WHERE id = ?", arrayOf(id.toString())).use { c ->
            if (c.moveToFirst()) Session(
                id = c.lng("id"), workoutId = c.lngNull("workoutId"), workoutName = c.s("workoutName"),
                dateEpochDay = c.lng("dateEpochDay"), startMillis = c.lng("startMillis"),
                endMillis = c.lngNull("endMillis"), pausedMillis = c.lng("pausedMillis"),
                pausedAt = c.lngNull("pausedAt"), completed = c.bool("completed"),
                feeling = c.int("feeling"), energy = c.int("energy"), motivation = c.int("motivation"),
                difficulty = c.int("difficulty"), notes = c.s("notes")
            ) else null
        }
    }

    fun openSession(): Session? {
        return db!!.rawQuery(
            "SELECT * FROM sessions WHERE endMillis IS NULL ORDER BY startMillis DESC LIMIT 1", null
        ).use { c ->
            if (c.moveToFirst()) session(c.lng("id")) else null
        }
    }

    fun startSession(workout: Workout?): Long {
        val now = System.currentTimeMillis()
        val id = db!!.insert("sessions", null, GymDb.cv(
            "workoutId" to workout?.id, "workoutName" to (workout?.name ?: "Treino livre"),
            "dateEpochDay" to DateUtils.today(), "startMillis" to now,
            "pausedMillis" to 0L, "completed" to false
        ))
        RefreshBus.bump()
        return id
    }

    fun updateSession(s: Session) {
        db!!.update("sessions", GymDb.cv(
            "workoutName" to s.workoutName, "endMillis" to s.endMillis,
            "pausedMillis" to s.pausedMillis, "pausedAt" to s.pausedAt,
            "completed" to s.completed, "feeling" to s.feeling, "energy" to s.energy,
            "motivation" to s.motivation, "difficulty" to s.difficulty, "notes" to s.notes
        ), "id = ?", arrayOf(s.id.toString()))
        RefreshBus.bump()
    }

    fun deleteSession(id: Long) {
        db!!.delete("set_logs", "sessionId = ?", arrayOf(id.toString()))
        db!!.delete("sessions", "id = ?", arrayOf(id.toString()))
        RefreshBus.bump()
    }

    // ===== Séries registradas =====

    fun setLogs(sessionId: Long): List<SetLog> {
        return db!!.rawQuery(
            "SELECT * FROM set_logs WHERE sessionId = ? ORDER BY timestamp", arrayOf(sessionId.toString())
        ).mapList { c ->
            SetLog(
                id = c.lng("id"), sessionId = c.lng("sessionId"), exerciseId = c.lng("exerciseId"),
                exerciseName = c.s("exerciseName"), muscleGroup = c.s("muscleGroup"),
                setNumber = c.int("setNumber"), reps = c.int("reps"), weight = c.dbl("weight"),
                rir = c.s("rir"), rpe = c.s("rpe"), completed = c.bool("completed"),
                note = c.s("note"), timestamp = c.lng("timestamp")
            )
        }
    }

    fun allLogs(): List<SetLog> {
        return db!!.rawQuery("SELECT * FROM set_logs ORDER BY timestamp", null).mapList { c ->
            SetLog(
                id = c.lng("id"), sessionId = c.lng("sessionId"), exerciseId = c.lng("exerciseId"),
                exerciseName = c.s("exerciseName"), muscleGroup = c.s("muscleGroup"),
                setNumber = c.int("setNumber"), reps = c.int("reps"), weight = c.dbl("weight"),
                rir = c.s("rir"), rpe = c.s("rpe"), completed = c.bool("completed"),
                note = c.s("note"), timestamp = c.lng("timestamp")
            )
        }
    }

    fun insertSetLog(l: SetLog): Long {
        val id = db!!.insert("set_logs", null, GymDb.cv(
            "sessionId" to l.sessionId, "exerciseId" to l.exerciseId, "exerciseName" to l.exerciseName,
            "muscleGroup" to l.muscleGroup, "setNumber" to l.setNumber, "reps" to l.reps,
            "weight" to l.weight, "rir" to l.rir, "rpe" to l.rpe,
            "completed" to l.completed, "note" to l.note, "timestamp" to l.timestamp
        ))
        RefreshBus.bump()
        return id
    }

    fun updateSetLog(l: SetLog) {
        db!!.update("set_logs", GymDb.cv(
            "reps" to l.reps, "weight" to l.weight, "rir" to l.rir, "rpe" to l.rpe,
            "completed" to l.completed, "note" to l.note
        ), "id = ?", arrayOf(l.id.toString()))
        RefreshBus.bump()
    }

    fun deleteSetLog(id: Long) {
        db!!.delete("set_logs", "id = ?", arrayOf(id.toString()))
        RefreshBus.bump()
    }

    /** Últimos registros por exercício de um treino, antes de um dado momento */
    fun lastLogsByExercise(workoutId: Long, beforeMillis: Long): Map<Long, SetLog> {
        val sIds = db!!.rawQuery(
            "SELECT id FROM sessions WHERE workoutId = ? AND startMillis < ?",
            arrayOf(workoutId.toString(), beforeMillis.toString())
        ).mapList { it.lng("id") }
        if (sIds.isEmpty()) return emptyMap()
        val inClause = sIds.joinToString(",") { it.toString() }
        val logs = db!!.rawQuery(
            "SELECT * FROM set_logs WHERE completed = 1 AND sessionId IN ($inClause) ORDER BY timestamp DESC", null
        ).mapList { c ->
            SetLog(
                id = c.lng("id"), sessionId = c.lng("sessionId"), exerciseId = c.lng("exerciseId"),
                exerciseName = c.s("exerciseName"), muscleGroup = c.s("muscleGroup"),
                setNumber = c.int("setNumber"), reps = c.int("reps"), weight = c.dbl("weight"),
                rir = c.s("rir"), rpe = c.s("rpe"), completed = c.bool("completed"),
                note = c.s("note"), timestamp = c.lng("timestamp")
            )
        }
        val out = HashMap<Long, SetLog>()
        for (l in logs) {
            if (l.exerciseId > 0 && !out.containsKey(l.exerciseId)) {
                // guarda o melhor (maior carga) dos últimos treinos de cada exercício
                val cur = out[l.exerciseId]
                if (cur == null || l.weight > cur.weight) out[l.exerciseId] = l
            }
        }
        return out
    }

    fun exerciseHistory(exerciseId: Long): List<SetLog> {
        return db!!.rawQuery(
            "SELECT * FROM set_logs WHERE exerciseId = ? AND completed = 1 ORDER BY timestamp",
            arrayOf(exerciseId.toString())
        ).mapList { c ->
            SetLog(
                id = c.lng("id"), sessionId = c.lng("sessionId"), exerciseId = c.lng("exerciseId"),
                exerciseName = c.s("exerciseName"), muscleGroup = c.s("muscleGroup"),
                setNumber = c.int("setNumber"), reps = c.int("reps"), weight = c.dbl("weight"),
                rir = c.s("rir"), rpe = c.s("rpe"), completed = c.bool("completed"),
                note = c.s("note"), timestamp = c.lng("timestamp")
            )
        }
    }

    // ===== Metas =====

    fun goals(): List<Goal> {
        return db!!.rawQuery("SELECT * FROM goals ORDER BY createdAt DESC", null).mapList { c ->
            Goal(
                id = c.lng("id"), title = c.s("title"), type = c.s("type"),
                target = c.dbl("target"), exerciseName = c.s("exerciseName"),
                createdAt = c.lng("createdAt"), done = c.bool("done")
            )
        }
    }

    fun insertGoal(g: Goal): Long {
        val id = db!!.insert("goals", null, GymDb.cv(
            "title" to g.title, "type" to g.type, "target" to g.target,
            "exerciseName" to g.exerciseName, "createdAt" to g.createdAt, "done" to g.done
        ))
        RefreshBus.bump()
        return id
    }

    fun updateGoal(g: Goal) {
        db!!.update("goals", GymDb.cv(
            "title" to g.title, "target" to g.target, "done" to g.done
        ), "id = ?", arrayOf(g.id.toString()))
        RefreshBus.bump()
    }

    fun deleteGoal(id: Long) {
        db!!.delete("goals", "id = ?", arrayOf(id.toString()))
        RefreshBus.bump()
    }

    // ===== Medidas =====

    fun measurements(): List<Measurement> {
        return db!!.rawQuery("SELECT * FROM measurements ORDER BY dateEpochDay DESC", null).mapList { c ->
            Measurement(
                id = c.lng("id"), dateEpochDay = c.lng("dateEpochDay"), weight = c.dbl("weight"),
                arm = c.dbl("arm"), chest = c.dbl("chest"), waist = c.dbl("waist"),
                hip = c.dbl("hip"), thigh = c.dbl("thigh"), calf = c.dbl("calf"), note = c.s("note")
            )
        }
    }

    fun insertMeasurement(m: Measurement): Long {
        val id = db!!.insert("measurements", null, GymDb.cv(
            "dateEpochDay" to m.dateEpochDay, "weight" to m.weight, "arm" to m.arm,
            "chest" to m.chest, "waist" to m.waist, "hip" to m.hip, "thigh" to m.thigh,
            "calf" to m.calf, "note" to m.note
        ))
        RefreshBus.bump()
        return id
    }

    fun deleteMeasurement(id: Long) {
        db!!.delete("measurements", "id = ?", arrayOf(id.toString()))
        RefreshBus.bump()
    }

    // ===== Lembretes =====

    fun reminders(): List<Reminder> {
        return db!!.rawQuery("SELECT * FROM reminders ORDER BY hour, minute", null).mapList { c ->
            Reminder(
                id = c.lng("id"), title = c.s("title"), type = c.s("type"),
                hour = c.int("hour"), minute = c.int("minute"),
                days = c.s("days"), enabled = c.bool("enabled")
            )
        }
    }

    fun reminder(id: Long): Reminder? {
        return db!!.rawQuery("SELECT * FROM reminders WHERE id = ?", arrayOf(id.toString())).use { c ->
            if (c.moveToFirst()) Reminder(
                id = c.lng("id"), title = c.s("title"), type = c.s("type"),
                hour = c.int("hour"), minute = c.int("minute"),
                days = c.s("days"), enabled = c.bool("enabled")
            ) else null
        }
    }

    fun insertReminder(r: Reminder): Long {
        val id = db!!.insert("reminders", null, GymDb.cv(
            "title" to r.title, "type" to r.type, "hour" to r.hour,
            "minute" to r.minute, "days" to r.days, "enabled" to r.enabled
        ))
        RefreshBus.bump()
        return id
    }

    fun updateReminder(r: Reminder) {
        db!!.update("reminders", GymDb.cv(
            "title" to r.title, "type" to r.type, "hour" to r.hour,
            "minute" to r.minute, "days" to r.days, "enabled" to r.enabled
        ), "id = ?", arrayOf(r.id.toString()))
        RefreshBus.bump()
    }

    fun deleteReminder(id: Long) {
        db!!.delete("reminders", "id = ?", arrayOf(id.toString()))
        RefreshBus.bump()
    }

    // ===== Anotações =====

    fun notes(): List<Note> {
        return db!!.rawQuery("SELECT * FROM notes ORDER BY updatedAt DESC", null).mapList { c ->
            Note(
                id = c.lng("id"), title = c.s("title"), content = c.s("content"),
                createdAt = c.lng("createdAt"), updatedAt = c.lng("updatedAt"), isFavorite = c.bool("isFavorite")
            )
        }
    }

    fun note(id: Long): Note? {
        return db!!.rawQuery("SELECT * FROM notes WHERE id = ?", arrayOf(id.toString())).use { c ->
            if (c.moveToFirst()) Note(
                id = c.lng("id"), title = c.s("title"), content = c.s("content"),
                createdAt = c.lng("createdAt"), updatedAt = c.lng("updatedAt"), isFavorite = c.bool("isFavorite")
            ) else null
        }
    }

    fun saveNote(n: Note): Long {
        val now = System.currentTimeMillis()
        val id = if (n.id > 0) {
            db!!.update("notes", GymDb.cv(
                "title" to n.title, "content" to n.content, "updatedAt" to now, "isFavorite" to n.isFavorite
            ), "id = ?", arrayOf(n.id.toString()))
            n.id
        } else {
            db!!.insert("notes", null, GymDb.cv(
                "title" to n.title, "content" to n.content,
                "createdAt" to now, "updatedAt" to now, "isFavorite" to n.isFavorite
            ))
        }
        RefreshBus.bump()
        return id
    }

    fun toggleNoteFavorite(n: Note) {
        db!!.update("notes", GymDb.cv("isFavorite" to !n.isFavorite), "id = ?", arrayOf(n.id.toString()))
        RefreshBus.bump()
    }

    fun deleteNote(id: Long) {
        db!!.delete("notes", "id = ?", arrayOf(id.toString()))
        RefreshBus.bump()
    }

    // ===== Dicas =====

    fun tips(): List<Tip> {
        return db!!.rawQuery("SELECT * FROM tips ORDER BY category, id", null).mapList { c ->
            Tip(
                id = c.lng("id"), category = c.s("category"), title = c.s("title"),
                content = c.s("content"), isFavorite = c.bool("isFavorite")
            )
        }
    }

    fun toggleTipFavorite(t: Tip) {
        db!!.update("tips", GymDb.cv("isFavorite" to !t.isFavorite), "id = ?", arrayOf(t.id.toString()))
        RefreshBus.bump()
    }

    // ===== Checklist =====

    fun checklist(category: String): List<ChecklistItem> {
        return db!!.rawQuery(
            "SELECT * FROM checklist_items WHERE category = ? ORDER BY orderIndex", arrayOf(category)
        ).mapList { c ->
            ChecklistItem(
                id = c.lng("id"), category = c.s("category"), label = c.s("label"),
                checked = c.bool("checked"), orderIndex = c.int("orderIndex")
            )
        }
    }

    fun addChecklistItem(category: String, label: String) {
        val max = db!!.rawQuery(
            "SELECT COALESCE(MAX(orderIndex), -1) FROM checklist_items WHERE category = ?", arrayOf(category)
        ).use { it.moveToFirst(); it.getInt(0) }
        db!!.insert("checklist_items", null, GymDb.cv(
            "category" to category, "label" to label, "checked" to false, "orderIndex" to max + 1
        ))
        RefreshBus.bump()
    }

    fun setChecklistChecked(id: Long, checked: Boolean) {
        db!!.update("checklist_items", GymDb.cv("checked" to checked), "id = ?", arrayOf(id.toString()))
        RefreshBus.bump()
    }

    fun deleteChecklistItem(id: Long) {
        db!!.delete("checklist_items", "id = ?", arrayOf(id.toString()))
        RefreshBus.bump()
    }

    fun resetChecklist(category: String) {
        db!!.update("checklist_items", GymDb.cv("checked" to false), "category = ?", arrayOf(category))
        RefreshBus.bump()
    }

    // ===== Fotos =====

    fun photos(): List<ProgressPhoto> {
        return db!!.rawQuery("SELECT * FROM photos ORDER BY dateEpochDay DESC, id DESC", null).mapList { c ->
            ProgressPhoto(id = c.lng("id"), dateEpochDay = c.lng("dateEpochDay"),
                path = c.s("path"), note = c.s("note"))
        }
    }

    fun insertPhoto(p: ProgressPhoto): Long {
        val id = db!!.insert("photos", null, GymDb.cv(
            "dateEpochDay" to p.dateEpochDay, "path" to p.path, "note" to p.note
        ))
        RefreshBus.bump()
        return id
    }

    fun deletePhoto(id: Long) {
        db!!.delete("photos", "id = ?", arrayOf(id.toString()))
        RefreshBus.bump()
    }

    // ===== Favoritos =====

    fun favoriteWorkouts(): List<Workout> = workouts().filter { it.isFavorite }
    fun favoriteExercises(): List<Exercise> = exercises().filter { it.isFavorite }
    fun favoriteTips(): List<Tip> = tips().filter { it.isFavorite }
    fun favoriteNotes(): List<Note> = notes().filter { it.isFavorite }

    fun toggleWorkoutFavorite(w: Workout) {
        db!!.update("workouts", GymDb.cv("isFavorite" to !w.isFavorite), "id = ?", arrayOf(w.id.toString()))
        RefreshBus.bump()
    }

    fun toggleExerciseFavorite(e: Exercise) {
        db!!.update("exercises", GymDb.cv("isFavorite" to !e.isFavorite), "id = ?", arrayOf(e.id.toString()))
        RefreshBus.bump()
    }

    // ===== Busca global =====

    data class SearchResults(
        val workouts: List<Workout> = emptyList(),
        val exercises: List<Exercise> = emptyList(),
        val notes: List<Note> = emptyList(),
        val tips: List<Tip> = emptyList(),
        val sessions: List<Session> = emptyList(),
        val logNotes: List<SetLog> = emptyList()
    )

    fun search(query: String): SearchResults {
        if (query.isBlank()) return SearchResults()
        val q = "%" + query.trim() + "%"
        val w = db!!.rawQuery(
            "SELECT * FROM workouts WHERE name LIKE ? OR description LIKE ? OR notes LIKE ? LIMIT 20", arrayOf(q, q, q)
        ).mapList { c -> Workout(c.lng("id"), c.s("name"), c.s("description"), c.intNull("dayOfWeek"),
            c.s("time"), c.int("estimatedMinutes"), c.s("notes"), c.int("colorIndex"), c.bool("isFavorite"), c.lng("createdAt")) }
        val e = db!!.rawQuery(
            "SELECT * FROM exercises WHERE name LIKE ? OR muscleGroup LIKE ? OR equipment LIKE ? LIMIT 20", arrayOf(q, q, q)
        ).mapList { c -> Exercise(c.lng("id"), c.s("name"), c.s("muscleGroup"), c.s("secondary"),
            c.s("equipment"), c.s("instructions"), c.s("tips"), c.s("mistakes"), c.s("personalNote"),
            c.bool("isCustom"), c.bool("isFavorite")) }
        val n = db!!.rawQuery(
            "SELECT * FROM notes WHERE title LIKE ? OR content LIKE ? LIMIT 20", arrayOf(q, q)
        ).mapList { c -> Note(c.lng("id"), c.s("title"), c.s("content"), c.lng("createdAt"), c.lng("updatedAt"), c.bool("isFavorite")) }
        val t = db!!.rawQuery(
            "SELECT * FROM tips WHERE title LIKE ? OR content LIKE ? LIMIT 20", arrayOf(q, q)
        ).mapList { c -> Tip(c.lng("id"), c.s("category"), c.s("title"), c.s("content"), c.bool("isFavorite")) }
        val sess = db!!.rawQuery(
            "SELECT * FROM sessions WHERE workoutName LIKE ? OR notes LIKE ? ORDER BY startMillis DESC LIMIT 20", arrayOf(q, q)
        ).mapList { c -> Session(c.lng("id"), c.lngNull("workoutId"), c.s("workoutName"), c.lng("dateEpochDay"),
            c.lng("startMillis"), c.lngNull("endMillis"), c.lng("pausedMillis"), c.lngNull("pausedAt"),
            c.bool("completed"), c.int("feeling"), c.int("energy"), c.int("motivation"), c.int("difficulty"), c.s("notes")) }
        val logs = db!!.rawQuery(
            "SELECT * FROM set_logs WHERE note LIKE ? AND note != '' ORDER BY timestamp DESC LIMIT 20", arrayOf(q)
        ).mapList { c -> SetLog(c.lng("id"), c.lng("sessionId"), c.lng("exerciseId"), c.s("exerciseName"),
            c.s("muscleGroup"), c.int("setNumber"), c.int("reps"), c.dbl("weight"), c.s("rir"), c.s("rpe"),
            c.bool("completed"), c.s("note"), c.lng("timestamp")) }
        return SearchResults(w, e, n, t, sess, logs)
    }
}
