package com.gymtrack.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GymDb(context: Context) : SQLiteOpenHelper(context, "gymtrack.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE workouts(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT DEFAULT '',
                dayOfWeek INTEGER,
                time TEXT DEFAULT '',
                estimatedMinutes INTEGER DEFAULT 0,
                notes TEXT DEFAULT '',
                colorIndex INTEGER DEFAULT 0,
                isFavorite INTEGER DEFAULT 0,
                createdAt INTEGER
            )
        """)
        db.execSQL("""
            CREATE TABLE exercises(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                muscleGroup TEXT DEFAULT '',
                secondary TEXT DEFAULT '',
                equipment TEXT DEFAULT '',
                instructions TEXT DEFAULT '',
                tips TEXT DEFAULT '',
                mistakes TEXT DEFAULT '',
                personalNote TEXT DEFAULT '',
                isCustom INTEGER DEFAULT 0,
                isFavorite INTEGER DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE TABLE workout_exercises(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                workoutId INTEGER NOT NULL,
                exerciseId INTEGER DEFAULT 0,
                exerciseName TEXT DEFAULT '',
                muscleGroup TEXT DEFAULT '',
                orderIndex INTEGER DEFAULT 0,
                sets INTEGER DEFAULT 3,
                repsMin INTEGER DEFAULT 8,
                repsMax INTEGER DEFAULT 12,
                weight REAL DEFAULT 0,
                restSeconds INTEGER DEFAULT 90,
                rir TEXT DEFAULT '',
                rpe TEXT DEFAULT '',
                tempo TEXT DEFAULT '',
                method TEXT DEFAULT '',
                notes TEXT DEFAULT ''
            )
        """)
        db.execSQL("""
            CREATE TABLE sessions(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                workoutId INTEGER,
                workoutName TEXT DEFAULT '',
                dateEpochDay INTEGER,
                startMillis INTEGER,
                endMillis INTEGER,
                pausedMillis INTEGER DEFAULT 0,
                pausedAt INTEGER,
                completed INTEGER DEFAULT 0,
                feeling INTEGER DEFAULT 0,
                energy INTEGER DEFAULT 0,
                motivation INTEGER DEFAULT 0,
                difficulty INTEGER DEFAULT 0,
                notes TEXT DEFAULT ''
            )
        """)
        db.execSQL("""
            CREATE TABLE set_logs(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sessionId INTEGER NOT NULL,
                exerciseId INTEGER DEFAULT 0,
                exerciseName TEXT DEFAULT '',
                muscleGroup TEXT DEFAULT '',
                setNumber INTEGER DEFAULT 1,
                reps INTEGER DEFAULT 0,
                weight REAL DEFAULT 0,
                rir TEXT DEFAULT '',
                rpe TEXT DEFAULT '',
                completed INTEGER DEFAULT 1,
                note TEXT DEFAULT '',
                timestamp INTEGER
            )
        """)
        db.execSQL("""
            CREATE TABLE goals(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT DEFAULT '',
                type TEXT DEFAULT 'semanal',
                target REAL DEFAULT 4,
                exerciseName TEXT DEFAULT '',
                createdAt INTEGER,
                done INTEGER DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE TABLE measurements(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dateEpochDay INTEGER,
                weight REAL DEFAULT -1,
                arm REAL DEFAULT -1,
                chest REAL DEFAULT -1,
                waist REAL DEFAULT -1,
                hip REAL DEFAULT -1,
                thigh REAL DEFAULT -1,
                calf REAL DEFAULT -1,
                note TEXT DEFAULT ''
            )
        """)
        db.execSQL("""
            CREATE TABLE reminders(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT DEFAULT '',
                type TEXT DEFAULT 'treino',
                hour INTEGER DEFAULT 7,
                minute INTEGER DEFAULT 30,
                days TEXT DEFAULT '1,2,3,4,5,6,7',
                enabled INTEGER DEFAULT 1
            )
        """)
        db.execSQL("""
            CREATE TABLE notes(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT DEFAULT '',
                content TEXT DEFAULT '',
                createdAt INTEGER,
                updatedAt INTEGER,
                isFavorite INTEGER DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE TABLE tips(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT DEFAULT '',
                title TEXT DEFAULT '',
                content TEXT DEFAULT '',
                isFavorite INTEGER DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE TABLE checklist_items(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT DEFAULT 'pre',
                label TEXT DEFAULT '',
                checked INTEGER DEFAULT 0,
                orderIndex INTEGER DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE TABLE photos(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dateEpochDay INTEGER,
                path TEXT DEFAULT '',
                note TEXT DEFAULT ''
            )
        """)
        Seed.seedExercises { c -> db.insert("exercises", null, c) }
        Seed.seedTips { c -> db.insert("tips", null, c) }
        Seed.seedChecklist { c -> db.insert("checklist_items", null, c) }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // versão 1 — nada a migrar
    }

    fun wipeAll() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (t in listOf(
                "workouts", "workout_exercises", "sessions", "set_logs", "goals",
                "measurements", "reminders", "notes", "checklist_items", "photos"
            )) {
                db.delete(t, null, null)
            }
            db.delete("exercises", "isCustom = 1", null)
            db.execSQL("UPDATE exercises SET personalNote = '', isFavorite = 0")
            db.execSQL("UPDATE tips SET isFavorite = 0")
            db.delete("checklist_items", null, null)
            Seed.seedChecklist { c -> db.insert("checklist_items", null, c); 0L }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    companion object {
        fun cv(vararg pairs: Pair<String, Any?>): ContentValues {
            val c = ContentValues()
            for ((k, v) in pairs) {
                when (v) {
                    null -> c.putNull(k)
                    is Boolean -> c.put(k, if (v) 1 else 0)
                    is Int -> c.put(k, v)
                    is Long -> c.put(k, v)
                    is Double -> c.put(k, v)
                    is String -> c.put(k, v)
                }
            }
            return c
        }
    }
}
