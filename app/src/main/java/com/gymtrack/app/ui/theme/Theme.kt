package com.gymtrack.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Lime = Color(0xFFA3E635)
val GreenDark = Color(0xFF16A34A)
val Orange = Color(0xFFF97316)
val Navy = Color(0xFF0B1220)
val NavyCard = Color(0xFF151E31)

private val LightColors = lightColorScheme(
    primary = Color(0xFF3F6E1E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEDA3),
    onPrimaryContainer = Color(0xFF10250A),
    secondary = Color(0xFFB45309),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF2B1700),
    background = Color(0xFFF7F9F2),
    onBackground = Color(0xFF191D14),
    surface = Color(0xFFF7F9F2),
    onSurface = Color(0xFF191D14),
    surfaceVariant = Color(0xFFE1E4D5),
    onSurfaceVariant = Color(0xFF44483D),
    outline = Color(0xFF75796C)
)

private val DarkColors = darkColorScheme(
    primary = Lime,
    onPrimary = Color(0xFF1D3701),
    primaryContainer = Color(0xFF37520F),
    onPrimaryContainer = Color(0xFFD3F5A3),
    secondary = Orange,
    onSecondary = Color(0xFF3B1D00),
    secondaryContainer = Color(0xFF5A2F00),
    onSecondaryContainer = Color(0xFFFFDCC2),
    background = Navy,
    onBackground = Color(0xFFE2E5DC),
    surface = Navy,
    onSurface = Color(0xFFE2E5DC),
    surfaceVariant = NavyCard,
    onSurfaceVariant = Color(0xFFC0C7B2),
    outline = Color(0xFF8A927E)
)

@Composable
fun GymTrackTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
