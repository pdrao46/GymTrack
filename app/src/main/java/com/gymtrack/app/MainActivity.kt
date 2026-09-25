package com.gymtrack.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gymtrack.app.data.Graph
import com.gymtrack.app.data.Settings
import com.gymtrack.app.data.SettingsRepo
import com.gymtrack.app.data.SettingsState
import com.gymtrack.app.ui.LocalAppSettings
import com.gymtrack.app.ui.nav.AppNav
import com.gymtrack.app.ui.theme.GymTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.init(applicationContext)
        SettingsRepo.startCollecting(applicationContext)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        setContent {
            val settings by SettingsRepo.flow(applicationContext)
                .collectAsState(initial = SettingsState.current)
            val dark = when (settings.theme) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }
            CompositionLocalProvider(LocalAppSettings provides settings) {
                GymTrackTheme(darkTheme = dark) {
                    AppNav()
                }
            }
        }
    }
}
