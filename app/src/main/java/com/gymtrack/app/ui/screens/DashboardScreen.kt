package com.gymtrack.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.*
import com.gymtrack.app.ui.nav.Routes
import com.gymtrack.app.ui.workoutPalette
import java.time.LocalDate
import kotlin.math.roundToInt

private data class WeekDayState(
    val iso: Int,
    val day: Long,
    val trained: Boolean,
    val planned: Boolean,
    val isToday: Boolean
)

@Composable
fun DashboardScreen(nav: NavHostController) {
    val tick = refreshTick()
    val settings = LocalAppSettings.current

    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var logs by remember { mutableStateOf<List<SetLog>>(emptyList()) }
    var wexMap by remember { mutableStateOf<Map<Long, List<WorkoutExercise>>>(emptyMap()) }
    var hasReminder by remember { mutableStateOf(false) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val w = Repo.workouts()
            val s = Repo.sessions(400)
            val l = Repo.allLogs()
            val map = HashMap<Long, List<WorkoutExercise>>()
            for (x in w) map[x.id] = Repo.workoutExercises(x.id)
            workouts = w; sessions = s; logs = l; wexMap = map
            hasReminder = Repo.reminders().any { it.enabled }
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

    // minutos treinados nesta semana (dados reais das sessões concluídas)
    val weekStart = DateUtils.weekStart(today)
    val weekMinutes = sessions
        .filter { it.dateEpochDay in weekStart..(weekStart + 6) && it.endMillis != null }
        .sumOf { Stats.durationOf(it) / 60000L }

    // evolução do volume: semana atual x anterior
    val wv = remember(tick) { Stats.weeklyVolume(logs, 2) }
    val volumeTrend = if (wv.size == 2 && wv[0].second > 0) {
        val diff = ((wv[1].second - wv[0].second) / wv[0].second * 100).roundToInt()
        when {
            diff > 0 -> "+$diff%"
            diff < 0 -> "$diff%"
            else -> "0%"
        }
    } else "—"

    val dayStates = remember(workouts, sessions, today) {
        val start = DateUtils.weekStart(today)
        (1..7).map { iso ->
            val day = start + iso - 1
            WeekDayState(
                iso = iso,
                day = day,
                trained = sessions.any { it.dateEpochDay == day },
                planned = workouts.any { it.dayOfWeek == iso },
                isToday = day == today
            )
        }
    }

    val greeting = if (settings.userName.isNotBlank()) "Olá, ${settings.userName} 👋" else "Olá! 👋"
    val insights = remember(tick) { Stats.insights(workouts, sessions, logs) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // ---------- Header ----------
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Vamos continuar sua evolução.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    DateUtils.fmtLong(today),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { nav.navigate(Routes.SEARCH) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    Icons.Filled.Search, "Buscar",
                    modifier = Modifier.size(19.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(4.dp))
            Box {
                IconButton(
                    onClick = { nav.navigate(Routes.REMINDERS) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Filled.Notifications, "Lembretes",
                        modifier = Modifier.size(19.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (hasReminder) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 9.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        // ---------- Sessão em andamento ----------
        AnimatedVisibility(
            visible = openSession != null,
            enter = fadeIn(tween(280)) + expandVertically(tween(300, easing = FastOutSlowInEasing))
        ) {
            Surface(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable {
                    openSession?.let { nav.navigate(Routes.activeWorkout(it.id)) }
                },
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.Timer, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Treino em andamento", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            openSession?.workoutName ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                            maxLines = 1
                        )
                    }
                    PillBadge(
                        "CONTINUAR →",
                        container = MaterialTheme.colorScheme.primary,
                        content = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // ---------- Treino de hoje ----------
        EnterSection(1) {
            SectionTitle("Treino de hoje")
            if (todayWorkout == null) {
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Filled.SelfImprovement, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Hoje é dia de descanso 😌", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Recupere ou escolha um treino na aba Treinos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    PrimaryBigButton("VER TREINOS", { nav.navigate(Routes.WORKOUTS) }, icon = Icons.Filled.FitnessCenter)
                }
            } else {
                val color = workoutPalette[todayWorkout.colorIndex % workoutPalette.size]
                val ratio = if (totalPlannedSets > 0) (doneSetsToday.toFloat() / totalPlannedSets) else 0f
                val pct = (ratio * 100).toInt()
                val doneToday = sessions.any { it.dateEpochDay == today && it.endMillis != null && it.completed }
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "TREINO DE HOJE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        if (doneToday) {
                            PillBadge(
                                "CONCLUÍDO ✓",
                                container = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                content = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        todayWorkout.name.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        buildString {
                            append("${todayWex.size} exercícios")
                            if (todayWorkout.estimatedMinutes > 0) append(" · ~${todayWorkout.estimatedMinutes} min")
                            if (todayWorkout.time.isNotBlank()) append(" · ${todayWorkout.time}")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$doneSetsToday/$totalPlannedSets séries",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "$pct%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    FilledProgressBar(ratio)
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
        }

        // ---------- Estatísticas rápidas ----------
        EnterSection(2) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                StatTile(
                    Icons.Filled.LocalFireDepartment,
                    "$streak",
                    "Sequência",
                    Modifier.weight(1f),
                    tint = Color(0xFFF97316)
                )
                StatTile(
                    Icons.Filled.FitnessCenter,
                    "${weekStats.completed}/${weekStats.planned}",
                    "Treinos",
                    Modifier.weight(1f)
                )
                StatTile(
                    Icons.Filled.Timer,
                    DateUtils.fmtDuration(weekMinutes),
                    "Tempo",
                    Modifier.weight(1f)
                )
                StatTile(
                    Icons.Filled.TrendingUp,
                    volumeTrend,
                    "Evolução",
                    Modifier.weight(1f)
                )
            }
        }

        // ---------- Progresso semanal ----------
        EnterSection(3) {
            SectionTitle("Progresso semanal")
            AppCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().height(78.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dayStates.forEach { ds ->
                        val target = when {
                            ds.trained -> 56.dp
                            ds.planned -> 32.dp
                            else -> 12.dp
                        }
                        val h by animateDpAsState(target, tween(600, easing = FastOutSlowInEasing), label = "dayBar")
                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Box(
                                Modifier
                                    .width(14.dp)
                                    .height(h)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(
                                        when {
                                            ds.trained -> MaterialTheme.colorScheme.primary
                                            ds.planned -> MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                                            else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (ds.trained) {
                                    Icon(
                                        Icons.Filled.Check, null,
                                        modifier = Modifier.size(10.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                DateUtils.dowShort(ds.iso).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (ds.isToday) FontWeight.Bold else FontWeight.Medium,
                                color = if (ds.isToday) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    LegendDot(MaterialTheme.colorScheme.primary, "Treinado")
                    LegendDot(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f), "Planejado")
                    LegendDot(MaterialTheme.colorScheme.surfaceContainerHighest, "Descanso")
                }
            }
        }

        // ---------- Próximo treino ----------
        EnterSection(4) {
            SectionTitle("Próximo treino")
            if (nextWorkout != null) {
                val (days, d, w) = nextWorkout
                AppCard(Modifier.fillMaxWidth(), onClick = { w?.let { nav.navigate(Routes.workoutDetail(it.id)) } }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                DateUtils.dowShort(d).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (days == 1) "Amanhã" else "Em $days dias",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                w?.name ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (w != null && w.time.isNotBlank()) {
                                Text(w.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                AppCard(Modifier.fillMaxWidth()) {
                    Text(
                        "Nenhum treino agendado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ---------- Insights ----------
        if (insights.isNotEmpty()) {
            EnterSection(5) {
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
        }

        // ---------- Atalhos ----------
        EnterSection(6) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppCard(Modifier.weight(1f), onClick = { nav.navigate(Routes.CHECKLIST) }) {
                    Box(
                        Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp)) }
                    Spacer(Modifier.height(9.dp))
                    Text("Checklist pré-treino", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                AppCard(Modifier.weight(1f), onClick = { nav.navigate(Routes.HISTORY) }) {
                    Box(
                        Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.History, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp)) }
                    Spacer(Modifier.height(9.dp))
                    Text("Histórico", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
