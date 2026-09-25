package com.gymtrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.AppCard
import com.gymtrack.app.ui.EmptyState
import com.gymtrack.app.ui.FilledProgressBar
import com.gymtrack.app.ui.LocalAppSettings
import com.gymtrack.app.ui.PrimaryBigButton
import com.gymtrack.app.ui.SectionTitle
import com.gymtrack.app.ui.StatCell
import com.gymtrack.app.ui.refreshTick
import com.gymtrack.app.ui.nav.Routes
import com.gymtrack.app.ui.workoutPalette
import java.time.LocalDate

@Composable
fun DashboardScreen(nav: NavHostController) {
    val tick = refreshTick()

    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var logs by remember { mutableStateOf<List<SetLog>>(emptyList()) }
    var wexMap by remember { mutableStateOf<Map<Long, List<WorkoutExercise>>>(emptyMap()) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val w = Repo.workouts()
            val s = Repo.sessions(400)
            val l = Repo.allLogs()
            val map = HashMap<Long, List<WorkoutExercise>>()
            for (x in w) map[x.id] = Repo.workoutExercises(x.id)
            workouts = w; sessions = s; logs = l; wexMap = map
        }
    }

    val today = DateUtils.today()
    val dow = DateUtils.dowIso(today)
    val todayWorkout = workouts.filter { it.dayOfWeek == dow }.firstOrNull()
    val openSession = sessions.firstOrNull { it.endMillis == null }
    val weekStats = Stats.currentWeek(workouts, sessions, logs)
    val streak = Stats.streak(sessions)
    val todayWex = todayWorkout?.let { wexMap[it.id] } ?: emptyList()
    val totalPlannedSets = todayWex.sumOf { it.sets }
    val doneSetsToday = logs.count { it.completed && it.timestamp >= Stats.startMillisOf(today) }

    val nextWorkout = (1..7).map { i -> Triple(i, (dow - 1 + i) % 7 + 1, i) }
        .map { (i, d, _) -> Triple(i, d, workouts.firstOrNull { it.dayOfWeek == d }) }
        .firstOrNull { it.third != null }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Olá! 👋", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    DateUtils.fmtLong(today),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { nav.navigate(Routes.SEARCH) }) {
                Icon(Icons.Filled.Search, "Buscar")
            }
        }

        if (openSession != null) {
            Surface(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable {
                    nav.navigate(Routes.activeWorkout(openSession.id))
                },
                color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Timer, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Treino em andamento", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(openSession.workoutName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Text("CONTINUAR →", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }

        SectionTitle("Seu treino de hoje")
        if (todayWorkout == null) {
            AppCard(Modifier.fillMaxWidth()) {
                EmptyState(
                    "Hoje é dia de descanso 😌\nAproveite para recuperar ou escolher um treino na aba Treinos.",
                    Icons.Filled.SelfImprovement
                )
                PrimaryBigButton("VER TREINOS", { nav.navigate(Routes.WORKOUTS) }, icon = Icons.Filled.FitnessCenter)
            }
        } else {
            val color = workoutPalette[todayWorkout.colorIndex % workoutPalette.size]
            val ratio = if (totalPlannedSets > 0) (doneSetsToday.toFloat() / totalPlannedSets) else 0f
            AppCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        todayWorkout.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (todaySessionsDone(sessions, today)) Icon(Icons.Filled.CheckCircle, "Concluído", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "${todayWex.size} exercícios · ${if (todayWorkout.estimatedMinutes > 0) "~${todayWorkout.estimatedMinutes} min" else "tempo livre"}${if (todayWorkout.time.isNotBlank()) " · ${todayWorkout.time}" else ""}",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "$doneSetsToday/$totalPlannedSets séries concluídas",
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                FilledProgressBar(ratio)
                Spacer(Modifier.height(6.dp))
                Text(
                    "${(ratio * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(14.dp))
                PrimaryBigButton(
                    if (openSession != null && openSession.workoutId == todayWorkout.id) "CONTINUAR TREINO" else "COMEÇAR TREINO",
                    {
                        Graph.launch {
                            val sid = openSession?.id ?: Repo.startSession(todayWorkout)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                nav.navigate(Routes.activeWorkout(sid))
                            }
                        }
                    },
                    icon = Icons.Filled.PlayArrow
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppCard(Modifier.weight(1.4f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Event, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Próximo treino", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(6.dp))
                if (nextWorkout != null) {
                    val (days, d, w) = nextWorkout
                    Text(
                        if (days == 1) "Amanhã" else "Em $days dias (${DateUtils.dowShort(d)})",
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                    )
                    Text(w!!.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                } else {
                    Text("Nenhum treino agendado", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            AppCard(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFFF97316), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sequência", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(6.dp))
                Text("🔥 $streak", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(if (streak == 1) "dia treinando" else "dias treinando", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        SectionTitle("Resumo da semana")
        AppCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCell("${weekStats.completed}/${weekStats.planned}", "treinos")
                StatCell("${weekStats.exercisesDone}", "exercícios")
                StatCell(if (weekStats.volume > 0) "${com.gymtrack.app.data.Disp.fmtKg(weekStats.volume)}kg" else "0kg", "volume")
                StatCell("${weekStats.frequency}%", "frequência")
            }
            Spacer(Modifier.height(8.dp))
            FilledProgressBar(weekStats.frequency / 100f)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                val start = DateUtils.weekStart(today)
                (1..7).forEach { iso ->
                    val day = start + iso - 1
                    val trained = sessions.any { it.dateEpochDay == day }
                    val planned = workouts.any { it.dayOfWeek == iso }
                    val isToday = day == today
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(if (isToday) 30.dp else 26.dp).clip(CircleShape).background(
                                when {
                                    trained -> MaterialTheme.colorScheme.primary
                                    planned -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                DateUtils.dowShort(iso).first().toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (trained) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("${LocalDate.ofEpochDay(day).dayOfMonth}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        val insights = remember(tick) { Stats.insights(workouts, sessions, logs) }
        if (insights.isNotEmpty()) {
            SectionTitle("Para você")
            AppCard(Modifier.fillMaxWidth()) {
                insights.forEach { txt ->
                    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                        Text("💡", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(8.dp))
                        Text(txt, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppCard(Modifier.weight(1f), onClick = { nav.navigate(Routes.CHECKLIST) }) {
                Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(6.dp))
                Text("Checklist pré-treino", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            AppCard(Modifier.weight(1f), onClick = { nav.navigate(Routes.HISTORY) }) {
                Icon(Icons.Filled.History, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(6.dp))
                Text("Histórico", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

private fun todaySessionsDone(sessions: List<Session>, today: Long): Boolean =
    sessions.any { it.dateEpochDay == today && it.endMillis != null && it.completed }
