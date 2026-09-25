package com.gymtrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.*
import com.gymtrack.app.ui.nav.Routes

@Composable
fun WorkoutDetailScreen(nav: NavHostController, workoutId: Long) {
    val tick = refreshTick()
    val settings = LocalAppSettings.current
    val context = LocalContext.current

    var workout by remember { mutableStateOf<Workout?>(null) }
    var wex by remember { mutableStateOf<List<WorkoutExercise>>(emptyList()) }
    var history by remember { mutableStateOf<List<Session>>(emptyList()) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(workoutId, tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            workout = Repo.workout(workoutId)
            wex = Repo.workoutExercises(workoutId)
            history = Repo.sessions(300).filter { it.workoutId == workoutId }
        }
    }

    val w = workout
    if (w == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Detalhes do treino", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { Graph.launch { Repo.toggleWorkoutFavorite(w) } }) {
                Icon(
                    if (w.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    "Favorito",
                    tint = if (w.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(workoutPalette[w.colorIndex % workoutPalette.size]))
            Spacer(Modifier.width(10.dp))
            Text(w.name.uppercase(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        if (w.description.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(w.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistMini(if (w.dayOfWeek != null) DateUtils.dowLong(w.dayOfWeek!!) else "Sem dia fixo")
            if (w.time.isNotBlank()) AssistMini(w.time)
            AssistMini(if (w.estimatedMinutes > 0) "~${w.estimatedMinutes} min" else "${wex.size} exercícios")
        }

        Spacer(Modifier.height(16.dp))
        PrimaryBigButton("COMEÇAR TREINO", {
            Graph.launch {
                val sid = Repo.startSession(w)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    nav.navigate(Routes.activeWorkout(sid))
                }
            }
        }, icon = Icons.Filled.PlayArrow)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TonalButton2("EDITAR", { nav.navigate(Routes.workoutEdit(w.id)) }, Modifier.weight(1f), Icons.Filled.Edit)
            TonalButton2("DUPLICAR", {
                Graph.launch {
                    Repo.duplicateWorkout(w.id)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Treino duplicado ✅", android.widget.Toast.LENGTH_SHORT).show()
                        nav.popBackStack()
                    }
                }
            }, Modifier.weight(1f), Icons.Filled.ContentCopy)
            TonalButton2("EXCLUIR", { confirmDelete = true }, Modifier.weight(1f), Icons.Filled.Delete)
        }

        Spacer(Modifier.height(18.dp))
        SectionTitle("Exercícios (${wex.size})")
        Spacer(Modifier.height(8.dp))
        if (wex.isEmpty()) {
            AppCard(Modifier.fillMaxWidth()) { EmptyState("Nenhum exercício. Edite o treino para adicionar.", Icons.Filled.ListAlt) }
        }
        wex.forEachIndexed { i, e ->
            AppCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(e.exerciseName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            buildString {
                                append("${e.sets} séries × ")
                                if (e.repsMin == e.repsMax) append("${e.repsMin}") else append("${e.repsMin}–${e.repsMax}")
                                append(" repetições")
                            },
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val details = mutableListOf<String>()
                        if (e.weight > 0) details.add("Carga: " + Disp.weight(e.weight, settings.unit))
                        details.add("Descanso: ${e.restSeconds}s")
                        if (e.rir.isNotBlank()) details.add("RIR ${e.rir}")
                        if (e.rpe.isNotBlank()) details.add("RPE ${e.rpe}")
                        if (e.method.isNotBlank()) details.add(e.method)
                        Text(details.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        if (e.notes.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text("📝 ${e.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (e.muscleGroup.isNotBlank()) {
                        Text(e.muscleGroup, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (history.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            SectionTitle("Histórico deste treino")
            Spacer(Modifier.height(8.dp))
            history.take(10).forEach { s ->
                AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.sessionDetail(s.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (s.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            null, tint = if (s.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(DateUtils.fmtDate(s.dateEpochDay), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            val dur = Stats.durationOf(s) / 60000L
                            Text(
                                if (s.endMillis != null) DateUtils.fmtDuration(dur) else "em andamento",
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }

    if (confirmDelete) {
        ConfirmDialog(
            "Excluir treino?",
            "\"${w.name}\" e seus exercícios serão removidos. O histórico de treinos realizados será mantido.",
            confirmText = "Excluir",
            onConfirm = {
                Graph.launch {
                    Repo.deleteWorkout(w.id)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { nav.popBackStack() }
                }
            },
            onDismiss = { confirmDelete = false }
        )
    }
}
