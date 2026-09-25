package com.gymtrack.app.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.*
import com.gymtrack.app.ui.nav.Routes

@Composable
fun HistoryScreen(nav: NavHostController) {
    val tick = refreshTick()
    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var volumes by remember { mutableStateOf<Map<Long, Double>>(emptyMap()) }
    var filter by remember { mutableStateOf(0) } // 0 todos, 1 concluídos, 2 incompletos

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val s = Repo.sessions(300)
            val v = HashMap<Long, Double>()
            for (x in s) v[x.id] = Stats.volumeOf(Repo.setLogs(x.id))
            sessions = s; volumes = v
        }
    }

    val filtered = when (filter) {
        1 -> sessions.filter { it.completed }
        2 -> sessions.filter { !it.completed }
        else -> sessions
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Histórico", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter == 0, onClick = { filter = 0 }, label = { Text("Todos") })
            FilterChip(selected = filter == 1, onClick = { filter = 1 }, label = { Text("✓ Concluídos") })
            FilterChip(selected = filter == 2, onClick = { filter = 2 }, label = { Text("Incompletos") })
        }
        Spacer(Modifier.height(8.dp))
        if (filtered.isEmpty()) {
            AppCard(Modifier.fillMaxWidth()) {
                EmptyState("Nenhum treino no histórico ainda. Bora treinar! 💪", Icons.Filled.History)
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(filtered.size) { i ->
                val s = filtered[i]
                AppCard(Modifier.fillMaxWidth(), onClick = { nav.navigate(Routes.sessionDetail(s.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (s.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            null,
                            tint = if (s.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.workoutName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                DateUtils.fmtLong(s.dateEpochDay),
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val dur = if (s.endMillis != null) DateUtils.fmtDuration(Stats.durationOf(s) / 60000L) else "em andamento"
                            Text(dur, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            val vol = volumes[s.id] ?: 0.0
                            if (vol > 0) Text("${com.gymtrack.app.data.Disp.fmtKg(vol)} kg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionDetailScreen(nav: NavHostController, sessionId: Long) {
    val tick = refreshTick()
    val settings = LocalAppSettings.current
    val context = LocalContext.current
    var session by remember { mutableStateOf<Session?>(null) }
    var logs by remember { mutableStateOf<List<SetLog>>(emptyList()) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId, tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            session = Repo.session(sessionId)
            logs = Repo.setLogs(sessionId)
        }
    }

    val s = session
    if (s == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val byExercise = logs.groupBy { it.exerciseName }
    val volume = Stats.volumeOf(logs)
    val sets = logs.count { it.completed }
    val reps = logs.filter { it.completed }.sumOf { it.reps }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Treino realizado", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, "Excluir", tint = MaterialTheme.colorScheme.error) }
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (s.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                null,
                tint = if (s.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(8.dp))
            Text(s.workoutName.uppercase(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        Text(
            "${DateUtils.fmtLong(s.dateEpochDay)} · ${if (s.endMillis != null) DateUtils.fmtDuration(Stats.durationOf(s) / 60000L) else "em andamento"}",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (s.feeling > 0) {
            Spacer(Modifier.height(6.dp))
            val emojis = listOf("😫", "😕", "🙂", "💪", "🔥")
            Text(
                "Sensação: ${emojis.getOrElse(s.feeling - 1) { "🙂" }}   ⚡ ${s.energy}   🎯 ${s.motivation}   😖 ${s.difficulty}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppCard(Modifier.weight(1f)) { StatCell("${sets}", "séries") }
            AppCard(Modifier.weight(1f)) { StatCell("$reps", "repetições") }
            AppCard(Modifier.weight(1f)) { StatCell(if (volume > 0) "${com.gymtrack.app.data.Disp.fmtKg(volume)}kg" else "0", "volume") }
        }

        if (s.notes.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            AppCard(Modifier.fillMaxWidth()) {
                Text("📝 Observações", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(s.notes, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(14.dp))
        SectionTitle("Exercícios e séries")
        Spacer(Modifier.height(8.dp))
        byExercise.forEach { (name, list) ->
            AppCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                list.sortedBy { it.setNumber }.forEach { l ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (l.completed) Icons.Filled.Check else Icons.Filled.Close,
                            null,
                            tint = if (l.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Série ${l.setNumber}: ${l.reps} reps × ${Disp.weight(l.weight, settings.unit)}" +
                                (if (l.rir.isNotBlank()) " · RIR ${l.rir}" else "") +
                                (if (l.rpe.isNotBlank()) " · RPE ${l.rpe}" else ""),
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f)
                        )
                    }
                    if (l.note.isNotBlank()) {
                        Text("   📝 ${l.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (s.workoutId != null && s.workoutId > 0 && Repo.workout(s.workoutId!!) != null) {
            Spacer(Modifier.height(14.dp))
            PrimaryBigButton("REPETIR ESTE TREINO", {
                Graph.launch {
                    val wid = s.workoutId
                    val w = if (wid != null) Repo.workout(wid) else null
                    val sid = Repo.startSession(w)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        nav.navigate(Routes.activeWorkout(sid))
                    }
                }
            }, icon = Icons.Filled.Replay)
        }
        Spacer(Modifier.height(30.dp))
    }

    if (confirmDelete) {
        ConfirmDialog("Excluir registro?", "O treino de ${DateUtils.fmtDate(s.dateEpochDay)} e todas as séries registradas serão apagados.",
            confirmText = "Excluir",
            onConfirm = {
                Graph.launch {
                    Repo.deleteSession(s.id)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { nav.popBackStack() }
                }
            },
            onDismiss = { confirmDelete = false })
    }
}
