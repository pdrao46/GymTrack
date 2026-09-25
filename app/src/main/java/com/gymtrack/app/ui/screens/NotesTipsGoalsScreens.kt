package com.gymtrack.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.*
import com.gymtrack.app.ui.nav.Routes

// ================= ANOTAÇÕES =================

@Composable
fun NotesScreen(nav: NavHostController) {
    val tick = refreshTick()
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var onlyFavs by remember { mutableStateOf(false) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { notes = Repo.notes() }
    }

    val filtered = notes.filter { n ->
        (!onlyFavs || n.isFavorite) &&
            (query.isBlank() || n.title.contains(query, true) || n.content.contains(query, true))
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
                Text("Anotações", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LabeledTextField(query, { query = it }, "🔍 Pesquisar por palavra...")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = !onlyFavs, onClick = { onlyFavs = false }, label = { Text("Todas (${notes.size})") })
                FilterChip(selected = onlyFavs, onClick = { onlyFavs = true }, label = { Text("⭐ Favoritas") })
            }
            Spacer(Modifier.height(8.dp))
            if (filtered.isEmpty()) {
                AppCard(Modifier.fillMaxWidth()) {
                    EmptyState("Nenhuma anotação. Anote objetivos, ideias e lembretes da academia!", Icons.Filled.EditNote)
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 90.dp)) {
                items(filtered.size) { i ->
                    val n = filtered[i]
                    AppCard(Modifier.fillMaxWidth(), onClick = { nav.navigate(Routes.noteEdit(n.id)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(n.title.ifBlank { "Sem título" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    n.content, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "editada em ${DateUtils.fmtDate(DateUtils.epochDayOfMillis(n.updatedAt))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                if (n.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                "Favorito",
                                tint = if (n.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable { Graph.launch { Repo.toggleNoteFavorite(n) } }
                            )
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { nav.navigate(Routes.noteEdit(-1L)) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(6.dp))
            Text("Nova")
        }
    }
}

@Composable
fun NoteEditScreen(nav: NavHostController, noteId: Long) {
    val isNew = noteId <= 0
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(isNew) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isNew) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val n = Repo.note(noteId)
                if (n != null) { title = n.title; content = n.content }
                loaded = true
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text(if (isNew) "Nova anotação" else "Editar anotação", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (!isNew) {
                IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, "Excluir", tint = MaterialTheme.colorScheme.error) }
            }
        }
        Spacer(Modifier.height(10.dp))
        LabeledTextField(title, { title = it.take(80) }, "Título")
        Spacer(Modifier.height(10.dp))
        LabeledTextField(content, { content = it.take(5000) }, "Escreva sua anotação, ideia ou objetivo...", minLines = 12, singleLine = false)
        Spacer(Modifier.height(16.dp))
        PrimaryBigButton("SALVAR ANOTAÇÃO", {
            Graph.launch {
                Repo.saveNote(Note(id = if (isNew) 0 else noteId, title = title, content = content))
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { nav.popBackStack() }
            }
        }, icon = Icons.Filled.Save)
    }

    if (confirmDelete) {
        ConfirmDialog("Excluir anotação?", "Esta anotação será apagada permanentemente.", confirmText = "Excluir",
            onConfirm = {
                Graph.launch {
                    Repo.deleteNote(noteId)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { nav.popBackStack() }
                }
            }, onDismiss = { confirmDelete = false })
    }
}

// ================= DICAS =================

@Composable
fun TipsScreen(nav: NavHostController) {
    val tick = refreshTick()
    var tips by remember { mutableStateOf<List<Tip>>(emptyList()) }
    var category by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { tips = Repo.tips() }
    }

    val filtered = if (category == null) tips else tips.filter { it.category == category }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Dicas 💡", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth().horizontalScrollable(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = category == null, onClick = { category = null }, label = { Text("Todas") })
            Seed.TIP_CATEGORIES.forEach { c ->
                FilterChip(selected = category == c, onClick = { category = if (category == c) null else c }, label = { Text(c) })
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(filtered.size) { i ->
                val t = filtered[i]
                AppCard(Modifier.fillMaxWidth(), onClick = { expanded = if (expanded == t.id) null else t.id }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(t.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(
                            if (t.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            "Favorito",
                            tint = if (t.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { Graph.launch { Repo.toggleTipFavorite(t) } }
                        )
                        Icon(
                            if (expanded == t.id) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            null, tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (expanded == t.id) {
                        Spacer(Modifier.height(8.dp))
                        Text(t.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Text(
                    "As dicas são educativas e de caráter geral — elas não substituem a orientação de profissionais de educação física e saúde.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun Modifier.horizontalScrollable(): Modifier =
    this.horizontalScroll(androidx.compose.foundation.rememberScrollState())

// ================= METAS =================

@Composable
fun GoalsScreen(nav: NavHostController) {
    val tick = refreshTick()
    var goals by remember { mutableStateOf<List<Goal>>(emptyList()) }
    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var logs by remember { mutableStateOf<List<SetLog>>(emptyList()) }
    var showNew by remember { mutableStateOf(false) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            goals = Repo.goals()
            sessions = Repo.sessions(500)
            logs = Repo.allLogs()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
                Text("Metas 🎯", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            if (goals.isEmpty()) {
                AppCard(Modifier.fillMaxWidth()) {
                    EmptyState("Crie metas como \"treinar 4 vezes por semana\" e acompanhe seu progresso automaticamente.", Icons.Filled.Flag)
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 90.dp)) {
                items(goals.size) { i ->
                    val g = goals[i]
                    val (cur, txt) = Stats.goalProgress(g, sessions, logs)
                    val ratio = if (g.target > 0) (cur / g.target).toFloat().coerceIn(0f, 1f) else 0f
                    val reached = cur >= g.target
                    AppCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RingProgress(
                                ratio = ratio,
                                modifier = Modifier.size(54.dp),
                                color = if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                strokeWidth = 5f
                            ) {
                                Text(
                                    "${(ratio * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        g.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (reached) {
                                        Spacer(Modifier.width(6.dp))
                                        PillBadge(
                                            "META CONCLUÍDA 🎯",
                                            container = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                            content = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(Modifier.height(3.dp))
                                Text(txt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                FilledProgressBar(ratio)
                            }
                            IconButton(onClick = { Graph.launch { Repo.deleteGoal(g.id) } }) {
                                Icon(Icons.Filled.Delete, "Excluir", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { showNew = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(6.dp))
            Text("Nova meta")
        }
    }

    if (showNew) {
        var type by remember { mutableStateOf(GoalType.WEEKLY) }
        var targetTxt by remember { mutableStateOf("4") }
        var title by remember { mutableStateOf("Treinar 4 vezes por semana") }
        var exerciseName by remember { mutableStateOf("") }
        var exercises by remember { mutableStateOf<List<String>>(emptyList()) }
        var pickExercise by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                exercises = Repo.exercises().map { it.name }
            }
        }

        fun suggestTitle(t: String, target: String): String = when (t) {
            GoalType.WEEKLY -> "Treinar ${target}x por semana"
            GoalType.TOTAL -> "Completar $target treinos"
            GoalType.STREAK -> "$target dias seguidos treinando"
            GoalType.VOLUME -> "Volume semanal de $target kg"
            else -> "Aumentar carga: $exerciseName"
        }

        AlertDialog(
            onDismissRequest = { showNew = false },
            title = { Text("Nova meta", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = type == GoalType.WEEKLY, onClick = { type = GoalType.WEEKLY; title = suggestTitle(type, targetTxt) }, label = { Text("Freq. semanal") })
                        FilterChip(selected = type == GoalType.TOTAL, onClick = { type = GoalType.TOTAL; title = suggestTitle(type, targetTxt) }, label = { Text("Total") })
                        FilterChip(selected = type == GoalType.STREAK, onClick = { type = GoalType.STREAK; title = suggestTitle(type, targetTxt) }, label = { Text("Sequência") })
                    }
                    Row(Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = type == GoalType.VOLUME, onClick = { type = GoalType.VOLUME; title = suggestTitle(type, targetTxt) }, label = { Text("Volume semanal") })
                        FilterChip(selected = type == GoalType.LOAD, onClick = { type = GoalType.LOAD; title = suggestTitle(type, targetTxt) }, label = { Text("Carga") })
                    }
                    if (type == GoalType.LOAD) {
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable { pickExercise = true }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Exercício", modifier = Modifier.weight(1f))
                            Text(exerciseName.ifBlank { "Escolher..." }, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    LabeledTextField(targetTxt, { targetTxt = it.filter { c -> c.isDigit() }.take(5) }, if (type == GoalType.VOLUME) "Alvo (kg por semana)" else "Alvo")
                    LabeledTextField(title, { title = it.take(80) }, "Título da meta")
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (type != GoalType.LOAD || exerciseName.isNotBlank()) {
                        Graph.launch {
                            Repo.insertGoal(
                                Goal(title = title.ifBlank { suggestTitle(type, targetTxt) }, type = type,
                                    target = targetTxt.toDoubleOrNull() ?: 4.0, exerciseName = exerciseName)
                            )
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { showNew = false }
                        }
                    }
                }) { Text("Criar meta") }
            },
            dismissButton = { TextButton(onClick = { showNew = false }) { Text("Cancelar") } }
        )

        if (pickExercise) {
            RadioPickerDialog("Escolher exercício", exercises, exerciseName,
                onSelect = { exerciseName = it; title = "Aumentar carga: $it"; pickExercise = false },
                onDismiss = { pickExercise = false })
        }
    }
}
