package com.gymtrack.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/* ============================================================================
 * Identidade visual GymTrack — dark premium
 *
 * Fundo quase preto, cards em cinza muito escuro, texto branco, texto
 * secundário em cinza e um único accent verde-limão usado com moderação
 * (botões, indicadores, progresso, estados ativos e números em destaque).
 * ========================================================================== */

/** Accent principal — verde-limão da marca */
val Lime = Color(0xFFA3E635)
/** Verde de apoio (sucesso) */
val GreenDark = Color(0xFF16A34A)
/** Laranja contido — fogo/sequência, usado com parcimônia */
val Orange = Color(0xFFF97316)
/** Fundo quase preto */
val Navy = Color(0xFF0A0A0B)
/** Card em cinza muito escuro */
val NavyCard = Color(0xFF16161A)

private val DarkColors = darkColorScheme(
    primary = Lime,
    onPrimary = Color(0xFF111B04),
    primaryContainer = Color(0xFF20300C),
    onPrimaryContainer = Color(0xFFCDF68B),
    secondary = Orange,
    onSecondary = Color(0xFF3B1D00),
    secondaryContainer = Color(0xFF2C1909),
    onSecondaryContainer = Color(0xFFFFD2AB),
    tertiary = Color(0xFFB8C7A1),
    onTertiary = Color(0xFF1A2410),
    background = Navy,
    onBackground = Color(0xFFF1F1F3),
    surface = Color(0xFF0F0F11),
    onSurface = Color(0xFFF1F1F3),
    surfaceVariant = NavyCard,
    onSurfaceVariant = Color(0xFF9B9BA5),
    outline = Color(0xFF2E2E35),
    outlineVariant = Color(0xFF212127),
    surfaceContainerLowest = Color(0xFF08080A),
    surfaceContainerLow = Color(0xFF121215),
    surfaceContainer = Color(0xFF141419),
    surfaceContainerHigh = Color(0xFF1C1C22),
    surfaceContainerHighest = Color(0xFF24242B),
    error = Color(0xFFF87171),
    onError = Color(0xFF400606),
    errorContainer = Color(0xFF4A1515),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF3F6E1E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEDA3),
    onPrimaryContainer = Color(0xFF10250A),
    secondary = Color(0xFFB45309),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF2B1700),
    tertiary = Color(0xFF4E6B2E),
    onTertiary = Color.White,
    background = Color(0xFFF6F7F2),
    onBackground = Color(0xFF1A1D15),
    surface = Color(0xFFFCFCFA),
    onSurface = Color(0xFF1A1D15),
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF4A4E42),
    outline = Color(0xFFDBDDD1),
    outlineVariant = Color(0xFFEBECE3),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F7F2),
    surfaceContainer = Color(0xFFF2F3EC),
    surfaceContainerHigh = Color(0xFFECEEE5),
    surfaceContainerHighest = Color(0xFFE6E8DE),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

/** Tipografia com hierarquia mais marcada: números e títulos com presença */
private val AppTypography: Typography = run {
    val base = Typography()
    Typography(
        displayLarge = base.displayLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
        displayMedium = base.displayMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp),
        displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp),
        headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp),
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(letterSpacing = 0.4.sp),
        labelMedium = base.labelMedium.copy(letterSpacing = 0.3.sp),
        labelSmall = base.labelSmall.copy(letterSpacing = 0.4.sp)
    )
}

@Composable
fun GymTrackTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
