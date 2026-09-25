package com.gymtrack.app.ui.nav

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymtrack.app.ui.screens.*

object Routes {
    const val HOME = "home"
    const val AGENDA = "agenda"
    const val WORKOUTS = "workouts"
    const val PROGRESS = "progress"
    const val MORE = "more"

    fun workoutDetail(id: Long) = "workout/$id"
    fun workoutEdit(id: Long) = "workoutEdit/$id"
    fun activeWorkout(sessionId: Long) = "active/$sessionId"
    fun exerciseDetail(id: Long) = "exercise/$id"
    fun sessionDetail(id: Long) = "session/$id"
    fun noteEdit(id: Long) = "note/$id"

    const val CATALOG = "catalog"
    const val TEMPLATES = "templates"
    const val HISTORY = "history"
    const val RECORDS = "records"
    const val MEASUREMENTS = "measurements"
    const val PHOTOS = "photos"
    const val NOTES = "notes"
    const val TIPS = "tips"
    const val GOALS = "goals"
    const val REMINDERS = "reminders"
    const val CHECKLIST = "checklist"
    const val SETTINGS = "settings"
    const val BACKUP = "backup"
    const val PRIVACY = "privacy"
    const val FAVORITES = "favorites"
    const val SEARCH = "search"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "Início", Icons.Filled.Home),
    Tab(Routes.AGENDA, "Agenda", Icons.Filled.CalendarMonth),
    Tab(Routes.WORKOUTS, "Treinos", Icons.Filled.FitnessCenter),
    Tab(Routes.PROGRESS, "Progresso", Icons.Filled.BarChart),
    Tab(Routes.MORE, "Mais", Icons.Filled.Menu)
)

/**
 * Barra de navegação inferior: baixa, escura, com indicador em pílula
 * animado e label claro na aba ativa.
 */
@Composable
private fun PremiumBottomBar(current: String?, onSelect: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(60.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = current == tab.route
                val iconColor by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    tween(220), label = "tabIcon"
                )
                val labelColor by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    tween(220), label = "tabLabel"
                )
                val pillWidth by animateDpAsState(if (selected) 52.dp else 30.dp, tween(260, easing = FastOutSlowInEasing), label = "tabPill")
                val interaction = remember { MutableInteractionSource() }
                Column(
                    Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(interactionSource = interaction, indication = null) {
                            if (current != tab.route) onSelect(tab.route)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(7.dp))
                    Box(
                        Modifier
                            .height(26.dp)
                            .width(pillWidth)
                            .clip(RoundedCornerShape(50))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (selected) 0.16f else 0f
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            tab.icon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(21.dp),
                            tint = iconColor
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = labelColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val showBar = current in tabs.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                PremiumBottomBar(current = current) { route ->
                    nav.navigate(route) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(230)) +
                    slideInVertically(tween(280, easing = FastOutSlowInEasing)) { it / 14 }
            },
            exitTransition = { fadeOut(tween(170)) },
            popEnterTransition = {
                fadeIn(tween(230)) +
                    slideInVertically(tween(280, easing = FastOutSlowInEasing)) { it / 14 }
            },
            popExitTransition = { fadeOut(tween(170)) }
        ) {
            composable(Routes.HOME) { DashboardScreen(nav) }
            composable(Routes.AGENDA) { AgendaScreen(nav) }
            composable(Routes.WORKOUTS) { WorkoutsScreen(nav) }
            composable(Routes.PROGRESS) { ProgressScreen(nav) }
            composable(Routes.MORE) { MoreScreen(nav) }

            composable(
                "workout/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { WorkoutDetailScreen(nav, it.arguments?.getLong("id") ?: 0L) }

            composable(
                "workoutEdit/{workoutId}",
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
            ) { WorkoutEditScreen(nav, it.arguments?.getLong("workoutId") ?: -1L) }

            composable(
                "active/{sessionId}",
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { ActiveWorkoutScreen(nav, it.arguments?.getLong("sessionId") ?: 0L) }

            composable(Routes.CATALOG) { ExerciseCatalogScreen(nav) }
            composable(
                "exercise/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { ExerciseDetailScreen(nav, it.arguments?.getLong("id") ?: 0L) }

            composable(Routes.TEMPLATES) { TemplatesScreen(nav) }
            composable(Routes.HISTORY) { HistoryScreen(nav) }
            composable(
                "session/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { SessionDetailScreen(nav, it.arguments?.getLong("id") ?: 0L) }

            composable(Routes.RECORDS) { RecordsScreen(nav) }
            composable(Routes.MEASUREMENTS) { MeasurementsScreen(nav) }
            composable(Routes.PHOTOS) { PhotosScreen(nav) }
            composable(Routes.NOTES) { NotesScreen(nav) }
            composable(
                "note/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { NoteEditScreen(nav, it.arguments?.getLong("id") ?: -1L) }
            composable(Routes.TIPS) { TipsScreen(nav) }
            composable(Routes.GOALS) { GoalsScreen(nav) }
            composable(Routes.REMINDERS) { RemindersScreen(nav) }
            composable(Routes.CHECKLIST) { ChecklistScreen(nav) }
            composable(Routes.SETTINGS) { SettingsScreen(nav) }
            composable(Routes.BACKUP) { BackupScreen(nav) }
            composable(Routes.PRIVACY) { PrivacyScreen(nav) }
            composable(Routes.FAVORITES) { FavoritesScreen(nav) }
            composable(Routes.SEARCH) { SearchScreen(nav) }
        }
    }
}

/** Navegação auxiliar */
fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true }
}
