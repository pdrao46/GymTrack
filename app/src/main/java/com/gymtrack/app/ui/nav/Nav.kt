package com.gymtrack.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val showBar = current in tabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = current == tab.route,
                            onClick = {
                                if (current != tab.route) {
                                    nav.navigate(tab.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, null) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
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
