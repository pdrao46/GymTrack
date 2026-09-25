package com.gymtrack.app.data

import org.json.JSONArray
import org.json.JSONObject

object Backup {

    fun exportJson(): String {
        val db = Graph.db!!.writableDatabase
        val root = JSONObject()
        root.put("app", "GymTrack")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val workouts = JSONArray()
        for (w in Repo.workouts()) workouts.put(JSONObject()
            .put("id", w.id).put("name", w.name).put("description", w.description)
            .put("dayOfWeek", w.dayOfWeek ?: JSONObject.NULL).put("time", w.time)
            .put("estimatedMinutes", w.estimatedMinutes).put("notes", w.notes)
            .put("colorIndex", w.colorIndex).put("isFavorite", w.isFavorite).put("createdAt", w.createdAt))
        root.put("workouts", workouts)

        val wes = JSONArray()
        for (w in Repo.workouts().map { it.id }) {
            for (we in Repo.workoutExercises(w)) wes.put(JSONObject()
                .put("workoutId", we.workoutId).put("exerciseId", we.exerciseId)
                .put("exerciseName", we.exerciseName).put("muscleGroup", we.muscleGroup)
                .put("orderIndex", we.orderIndex).put("sets", we.sets).put("repsMin", we.repsMin)
                .put("repsMax", we.repsMax).put("weight", we.weight).put("restSeconds", we.restSeconds)
                .put("rir", we.rir).put("rpe", we.rpe).put("tempo", we.tempo)
                .put("method", we.method).put("notes", we.notes))
        }
        root.put("workoutExercises", wes)

        val sess = JSONArray()
        for (s in Repo.sessions(100000)) sess.put(JSONObject()
            .put("id", s.id).put("workoutId", s.workoutId ?: JSONObject.NULL).put("workoutName", s.workoutName)
            .put("dateEpochDay", s.dateEpochDay).put("startMillis", s.startMillis)
            .put("endMillis", s.endMillis ?: JSONObject.NULL).put("pausedMillis", s.pausedMillis)
            .put("pausedAt", s.pausedAt ?: JSONObject.NULL).put("completed", s.completed)
            .put("feeling", s.feeling).put("energy", s.energy).put("motivation", s.motivation)
            .put("difficulty", s.difficulty).put("notes", s.notes))
        root.put("sessions", sess)

        val logs = JSONArray()
        for (l in Repo.allLogs()) logs.put(JSONObject()
            .put("sessionId", l.sessionId).put("exerciseId", l.exerciseId)
            .put("exerciseName", l.exerciseName).put("muscleGroup", l.muscleGroup)
            .put("setNumber", l.setNumber).put("reps", l.reps).put("weight", l.weight)
            .put("rir", l.rir).put("rpe", l.rpe).put("completed", l.completed)
            .put("note", l.note).put("timestamp", l.timestamp))
        root.put("setLogs", logs)

        val goals = JSONArray()
        for (g in Repo.goals()) goals.put(JSONObject()
            .put("title", g.title).put("type", g.type).put("target", g.target)
            .put("exerciseName", g.exerciseName).put("createdAt", g.createdAt).put("done", g.done))
        root.put("goals", goals)

        val meas = JSONArray()
        for (m in Repo.measurements()) meas.put(JSONObject()
            .put("dateEpochDay", m.dateEpochDay).put("weight", m.weight).put("arm", m.arm)
            .put("chest", m.chest).put("waist", m.waist).put("hip", m.hip).put("thigh", m.thigh)
            .put("calf", m.calf).put("note", m.note))
        root.put("measurements", meas)

        val rems = JSONArray()
        for (r in Repo.reminders()) rems.put(JSONObject()
            .put("title", r.title).put("type", r.type).put("hour", r.hour).put("minute", r.minute)
            .put("days", r.days).put("enabled", r.enabled))
        root.put("reminders", rems)

        val notes = JSONArray()
        for (n in Repo.notes()) notes.put(JSONObject()
            .put("title", n.title).put("content", n.content).put("createdAt", n.createdAt)
            .put("updatedAt", n.updatedAt).put("isFavorite", n.isFavorite))
        root.put("notes", notes)

        val checks = JSONArray()
        root.put("checklist", checks)

        return root.toString(2)
    }

    fun importJson(text: String): Int {
        val root = JSONObject(text)
        val db = Graph.db!!.writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("DELETE FROM workouts")
            db.execSQL("DELETE FROM workout_exercises")
            db.execSQL("DELETE FROM sessions")
            db.execSQL("DELETE FROM set_logs")
            db.execSQL("DELETE FROM goals")
            db.execSQL("DELETE FROM measurements")
            db.execSQL("DELETE FROM reminders")
            db.execSQL("DELETE FROM notes")

            fun optLong(o: JSONObject, k: String): Long? = if (o.isNull(k)) null else o.getLong(k)
            fun optDouble(o: JSONObject, k: String): Double? = if (o.isNull(k)) null else o.getDouble(k)

            for (arr in arrayOf(root.optJSONArray("workouts"))) {
                if (arr != null) for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    db.insert("workouts", null, GymDb.cv(
                        "name" to o.getString("name"), "description" to o.optString("description"),
                        "dayOfWeek" to optLong(o, "dayOfWeek"), "time" to o.optString("time"),
                        "estimatedMinutes" to o.optInt("estimatedMinutes"), "notes" to o.optString("notes"),
                        "colorIndex" to o.optInt("colorIndex"), "isFavorite" to o.optBoolean("isFavorite"),
                        "createdAt" to o.optLong("createdAt")
                    ))
                }
            }
            root.optJSONArray("workoutExercises")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    db.insert("workout_exercises", null, GymDb.cv(
                        "workoutId" to o.getLong("workoutId"), "exerciseId" to o.optLong("exerciseId"),
                        "exerciseName" to o.optString("exerciseName"), "muscleGroup" to o.optString("muscleGroup"),
                        "orderIndex" to o.optInt("orderIndex"), "sets" to o.optInt("sets"),
                        "repsMin" to o.optInt("repsMin"), "repsMax" to o.optInt("repsMax"),
                        "weight" to o.optDouble("weight"), "restSeconds" to o.optInt("restSeconds"),
                        "rir" to o.optString("rir"), "rpe" to o.optString("rpe"),
                        "tempo" to o.optString("tempo"), "method" to o.optString("method"),
                        "notes" to o.optString("notes")
                    ))
                }
            }
            root.optJSONArray("sessions")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    db.insert("sessions", null, GymDb.cv(
                        "workoutId" to optLong(o, "workoutId"), "workoutName" to o.optString("workoutName"),
                        "dateEpochDay" to o.optLong("dateEpochDay"), "startMillis" to o.optLong("startMillis"),
                        "endMillis" to optLong(o, "endMillis"), "pausedMillis" to o.optLong("pausedMillis"),
                        "pausedAt" to optLong(o, "pausedAt"), "completed" to o.optBoolean("completed"),
                        "feeling" to o.optInt("feeling"), "energy" to o.optInt("energy"),
                        "motivation" to o.optInt("motivation"), "difficulty" to o.optInt("difficulty"),
                        "notes" to o.optString("notes")
                    ))
                }
            }
            root.optJSONArray("setLogs")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    db.insert("set_logs", null, GymDb.cv(
                        "sessionId" to o.getLong("sessionId"), "exerciseId" to o.optLong("exerciseId"),
                        "exerciseName" to o.optString("exerciseName"), "muscleGroup" to o.optString("muscleGroup"),
                        "setNumber" to o.optInt("setNumber"), "reps" to o.optInt("reps"),
                        "weight" to (optDouble(o, "weight") ?: 0.0), "rir" to o.optString("rir"),
                        "rpe" to o.optString("rpe"), "completed" to o.optBoolean("completed", true),
                        "note" to o.optString("note"), "timestamp" to o.optLong("timestamp")
                    ))
                }
            }
            root.optJSONArray("goals")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    db.insert("goals", null, GymDb.cv(
                        "title" to o.optString("title"), "type" to o.optString("type", GoalType.WEEKLY),
                        "target" to o.optDouble("target", 4.0), "exerciseName" to o.optString("exerciseName"),
                        "createdAt" to o.optLong("createdAt"), "done" to o.optBoolean("done")
                    ))
                }
            }
            root.optJSONArray("measurements")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    db.insert("measurements", null, GymDb.cv(
                        "dateEpochDay" to o.optLong("dateEpochDay"), "weight" to o.optDouble("weight", -1.0),
                        "arm" to o.optDouble("arm", -1.0), "chest" to o.optDouble("chest", -1.0),
                        "waist" to o.optDouble("waist", -1.0), "hip" to o.optDouble("hip", -1.0),
                        "thigh" to o.optDouble("thigh", -1.0), "calf" to o.optDouble("calf", -1.0),
                        "note" to o.optString("note")
                    ))
                }
            }
            root.optJSONArray("reminders")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    db.insert("reminders", null, GymDb.cv(
                        "title" to o.optString("title"), "type" to o.optString("type", "treino"),
                        "hour" to o.optInt("hour", 7), "minute" to o.optInt("minute", 30),
                        "days" to o.optString("days", "1,2,3,4,5,6,7"), "enabled" to o.optBoolean("enabled", true)
                    ))
                }
            }
            root.optJSONArray("notes")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    db.insert("notes", null, GymDb.cv(
                        "title" to o.optString("title"), "content" to o.optString("content"),
                        "createdAt" to o.optLong("createdAt"), "updatedAt" to o.optLong("updatedAt"),
                        "isFavorite" to o.optBoolean("isFavorite")
                    ))
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        RefreshBus.bump()
        return 0
    }

    fun exportCsv(): String {
        val sb = StringBuilder()
        sb.append("data;treino;duracao_min;concluido;exercicio;serie;reps;carga_kg;rir;rpe;observacao\n")
        val sessions = Repo.sessions(100000).sortedBy { it.startMillis }
        for (s in sessions) {
            val logs = Repo.setLogs(s.id)
            val dur = (Stats.durationOf(s) / 60000L)
            if (logs.isEmpty()) {
                sb.append("${DateUtils.fmtDate(s.dateEpochDay)};${s.workoutName};$dur;${if (s.completed) "sim" else "nao"};;;;;;\n")
            } else {
                for (l in logs) {
                    val note = l.note.replace(";", ",").replace("\n", " ")
                    sb.append(
                        "${DateUtils.fmtDate(s.dateEpochDay)};${s.workoutName};$dur;${if (s.completed) "sim" else "nao"};" +
                            "${l.exerciseName.replace(";", ",")};${l.setNumber};${l.reps};${Disp.fmtKg(l.weight)};" +
                            "${l.rir};${l.rpe};$note\n"
                    )
                }
            }
        }
        return sb.toString()
    }
}
