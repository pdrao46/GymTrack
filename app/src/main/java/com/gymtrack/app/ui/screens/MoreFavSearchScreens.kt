package com.gymtrack.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.*
import com.gymtrack.app.ui.nav.Routes

// ================= MAIS =================

@Composable
fun MoreScreen(nav: NavHostController) {
    val settings = LocalAppSettings.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Text("Mais", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "${settings.appName} — tudo sobre seus treinos em um só lugar",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))

        MenuGroup("ACOMPANHAMENTO", listOf(
            Triple("Histórico", Icons.Filled.History) { nav.navigate(Routes.HISTORY) },
            Triple("Metas 🎯", Icons.Filled.Flag) { nav.navigate(Routes.GOALS) },
            Triple("Recordes 🏆", Icons.Filled.EmojiEvents) { nav.navigate(Routes.RECORDS) },
            Triple("Medidas corporais", Icons.Filled.Straighten) { nav.navigate(Routes.MEASUREMENTS) },
            Triple("Fotos de evolução", Icons.Filled.PhotoCamera) { nav.navigate(Routes.PHOTOS) }
        ))
        MenuGroup("ORGANIZAÇÃO", listOf(
            Triple("Anotações", Icons.Filled.EditNote) { nav.navigate(Routes.NOTES) },
            Triple("Dicas 💡", Icons.Filled.Lightbulb) { nav.navigate(Routes.TIPS) },
            Triple("Checklist pré/pós-treino", Icons.Filled.Checklist) { nav.navigate(Routes.CHECKLIST) },
            Triple("Lembretes 🔔", Icons.Filled.Alarm) { nav.navigate(Routes.REMINDERS) },
            Triple("Favoritos ⭐", Icons.Filled.Star) { nav.navigate(Routes.FAVORITES) },
            Triple("Banco de exercícios", Icons.Filled.ListAlt) { nav.navigate(Routes.CATALOG) },
            Triple("Modelos prontos", Icons.Filled.AutoAwesome) { nav.navigate(Routes.TEMPLATES) }
        ))
        MenuGroup("SISTEMA", listOf(
            Triple("Buscar", Icons.Filled.Search) { nav.navigate(Routes.SEARCH) },
            Triple("Configurações", Icons.Filled.Settings) { nav.navigate(Routes.SETTINGS) },
            Triple("Backup (exportar/importar)", Icons.Filled.SaveAlt) { nav.navigate(Routes.BACKUP) },
            Triple("Privacidade e dados", Icons.Filled.Lock) { nav.navigate(Routes.PRIVACY) }
        ))
        Spacer(Modifier.height(20.dp))
        Text(
            "GymTrack v1.0.0 · 100% offline · seus dados ficam no seu aparelho 🔒",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MenuGroup(title: String, items: List<Triple<String, ImageVector, () -> Unit>>) {
    SectionTitle(title)
    Spacer(Modifier.height(6.dp))
    AppCard(Modifier.fillMaxWidth()) {
        items.forEach { (label, icon, action) ->
            Row(
                Modifier.fillMaxWidth().clickable { action() }.padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(14.dp))
                Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}

// ================= FAVORITOS =================

@Composable
fun FavoritesScreen(nav: NavHostController) {
    val tick = refreshTick()
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var tips by remember { mutableStateOf<List<Tip>>(emptyList()) }
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            workouts = Repo.favoriteWorkouts()
            exercises = Repo.favoriteExercises()
            tips = Repo.favoriteTips()
            notes = Repo.favoriteNotes()
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Favoritos ⭐", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))

        if (workouts.isEmpty() && exercises.isEmpty() && tips.isEmpty() && notes.isEmpty()) {
            AppCard(Modifier.fillMaxWidth()) {
                EmptyState("Nada favoritado ainda. Toque na ⭐ de treinos, exercícios, dicas e anotações para vê-los aqui.", Icons.Filled.Star)
            }
        }
        if (workouts.isNotEmpty()) {
            SectionTitle("Treinos")
            workouts.forEach { w ->
                AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.workoutDetail(w.id)) }) {
                    Text(w.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("${if (w.dayOfWeek != null) DateUtils.dowLong(w.dayOfWeek!!) else "Sem dia fixo"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        if (exercises.isNotEmpty()) {
            SectionTitle("Exercícios")
            exercises.forEach { e ->
                AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.exerciseDetail(e.id)) }) {
                    Text(e.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(e.muscleGroup, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        if (tips.isNotEmpty()) {
            SectionTitle("Dicas")
            tips.forEach { t ->
                AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(t.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(t.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        if (notes.isNotEmpty()) {
            SectionTitle("Anotações")
            notes.forEach { n ->
                AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.noteEdit(n.id)) }) {
                    Text(n.title.ifBlank { "Sem título" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(n.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ================= BUSCA GLOBAL =================

@Composable
fun SearchScreen(nav: NavHostController) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<Repo.SearchResults?>(null) }
    val tick = refreshTick()

    LaunchedEffect(query, tick) {
        if (query.isBlank()) {
            results = null
        } else {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                results = Repo.search(query)
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Buscar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        LabeledTextField(query, { query = it.take(80) }, "Exercício, treino, data, grupo, observação...")
        Spacer(Modifier.height(12.dp))
        val r = results
        if (r == null) {
            AppCard(Modifier.fillMaxWidth()) {
                EmptyState("Digite para buscar em treinos, exercícios, anotações, dicas, histórico e observações.", Icons.Filled.Search)
            }
        } else {
            val total = r.workouts.size + r.exercises.size + r.notes.size + r.tips.size + r.sessions.size + r.logNotes.size
            if (total == 0) {
                AppCard(Modifier.fillMaxWidth()) { EmptyState("Nada encontrado para \"$query\".", Icons.Filled.SearchOff) }
            } else {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    if (r.workouts.isNotEmpty()) {
                        SectionTitle("Treinos")
                        r.workouts.forEach { w ->
                            AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.workoutDetail(w.id)) }) {
                                Text(w.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                    if (r.exercises.isNotEmpty()) {
                        SectionTitle("Exercícios")
                        r.exercises.forEach { e ->
                            AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.exerciseDetail(e.id)) }) {
                                Text(e.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(e.muscleGroup, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (r.sessions.isNotEmpty()) {
                        SectionTitle("Histórico")
                        r.sessions.forEach { s ->
                            AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.sessionDetail(s.id)) }) {
                                Text("${DateUtils.fmtDate(s.dateEpochDay)} — ${s.workoutName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                    if (r.logNotes.isNotEmpty()) {
                        SectionTitle("Observações registradas")
                        r.logNotes.forEach { l ->
                            AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.sessionDetail(l.sessionId)) }) {
                                Text("${l.exerciseName} — série ${l.setNumber}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(l.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (r.notes.isNotEmpty()) {
                        SectionTitle("Anotações")
                        r.notes.forEach { n ->
                            AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.noteEdit(n.id)) }) {
                                Text(n.title.ifBlank { "Sem título" }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                    if (r.tips.isNotEmpty()) {
                        SectionTitle("Dicas")
                        r.tips.forEach { t ->
                            AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(t.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(t.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
