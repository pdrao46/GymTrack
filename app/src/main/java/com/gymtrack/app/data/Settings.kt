package com.gymtrack.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gymtrack.app.rem.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "gymtrack_settings")

data class Settings(
    val appName: String = "GymTrack",
    val theme: Int = 0, // 0 sistema, 1 claro, 2 escuro
    val unit: String = "kg", // kg | lb
    val firstDaySunday: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val comfortableCards: Boolean = true
)

/** Estado global observável das configurações (para uso em receivers e UI) */
object SettingsState {
    @Volatile var current: Settings = Settings()
}

object SettingsRepo {
    val KEY_NAME = stringPreferencesKey("app_name")
    val KEY_THEME = intPreferencesKey("theme")
    val KEY_UNIT = stringPreferencesKey("unit")
    val KEY_FIRST_SUNDAY = booleanPreferencesKey("first_day_sunday")
    val KEY_NOTIF = booleanPreferencesKey("notifications")
    val KEY_SOUND = booleanPreferencesKey("sound")
    val KEY_VIBRATE = booleanPreferencesKey("vibrate")
    val KEY_CARDS = booleanPreferencesKey("comfortable_cards")

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun flow(context: Context): Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            appName = p[KEY_NAME] ?: "GymTrack",
            theme = p[KEY_THEME] ?: 0,
            unit = p[KEY_UNIT] ?: "kg",
            firstDaySunday = p[KEY_FIRST_SUNDAY] ?: false,
            notificationsEnabled = p[KEY_NOTIF] ?: true,
            soundEnabled = p[KEY_SOUND] ?: true,
            vibrateEnabled = p[KEY_VIBRATE] ?: true,
            comfortableCards = p[KEY_CARDS] ?: true
        )
    }

    fun startCollecting(context: Context) {
        appScope.launch {
            flow(context.applicationContext).collect { s ->
                SettingsState.current = s
                if (!s.notificationsEnabled) ReminderScheduler.cancelAll(context.applicationContext)
                else ReminderScheduler.rescheduleAll(context.applicationContext)
            }
        }
    }

    suspend fun save(context: Context, transform: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit { p -> transform(p) }
    }
}
