package com.gymtrack.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class GymTrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDERS, "Lembretes de treino", NotificationManager.IMPORTANCE_HIGH)
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_WORKOUT, "Treino e descanso", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    companion object {
        const val CHANNEL_REMINDERS = "lembretes"
        const val CHANNEL_WORKOUT = "treino"
    }
}
