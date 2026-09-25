package com.gymtrack.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.*
import com.gymtrack.app.ui.nav.Routes
import java.time.LocalDate

@Composable
fun AgendaScreen(nav: NavHostController) {
    val tick = refreshTick()
    var mode by remember { mutableStateOf(0) } // 0 = semana, 1 = mês

    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            workouts = Repo.workouts()
            sessions = Repo.sessions(500)
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Agenda", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { nav.navigate(Routes.SEARCH) }) { Icon(Icons.Filled.Search, "Buscar") }
        }
        TabRow(selectedTabIndex = mode) {
            Tab(selected = mode == 0, onClick = { mode = 0 }, text = { Text("Semana") })
            Tab(selected = mode == 1, onClick = { mode = 1 }, text = { Text("Frequência") })
        }
        Spacer(Modifier.height(12.dp))
        if (mode == 0) {
            WeekAgenda(nav, workouts, sessions)
        } else {
            FrequencyCalendar(workouts, sessions)
        }
    }
}

@Composable
private fun WeekAgenda(nav: NavHostController, workouts: List<Workout>, sessions: List<Session>) {
    val today = DateUtils.today()
    val start = DateUtils.weekStart(today)
    val tick = refreshTick()
    val wexCount = remember(tick) { mutableStateOf<Map<Long, Int>>(emptyMap()) }
    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val m = HashMap<Long, Int>()
            for (w in workouts) m[w.id] = Repo.workoutExercises(w.id).size
            wexCount.value = m
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        (1..7).forEach { iso ->
            val day = start + iso - 1
            val dayWorkouts = workouts.filter { it.dayOfWeek == iso }
            val trained = sessions.any { it.dateEpochDay == day && it.endMillis != null }
            val incomplete = sessions.any { it.dateEpochDay == day && it.endMillis == null }
            val isToday = day == today
            val past = day < today
            val status = when {
                trained -> 'C' // concluído
                incomplete -> 'I' // em andamento
                dayWorkouts.isNotEmpty() && past -> 'X' // faltou
                dayWorkouts.isNotEmpty() -> 'P' // planejado
                else -> 'D' // descanso
            }
            val icon: androidx.compose.ui.graphics.vector.ImageVector
            val tint: androidx.compose.ui.graphics.Color
            val label: String
            when (status) {
                'C' -> { icon = Icons.Filled.CheckCircle; tint = MaterialTheme.colorScheme.primary; label = "Concluído" }
                'I' -> { icon = Icons.Filled.RadioButtonUnchecked; tint = MaterialTheme.colorScheme.secondary; label = "Em andamento" }
                'X' -> { icon = Icons.Filled.Cancel; tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f); label = "Não realizado" }
                'P' -> { icon = Icons.Filled.Schedule; tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f); label = "Planejado" }
                else -> { icon = Icons.Filled.Bedtime; tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f); label = "Descanso" }
            }
            AppCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.width(74.dp)) {
                        Text(DateUtils.dowLong(iso), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            DateUtils.fmtDate(day) + if (isToday) " · hoje" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        if (dayWorkouts.isEmpty()) {
                            Text("Descanso 😴", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            dayWorkouts.forEach { w ->
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                        .clickable { nav.navigate(Routes.workoutDetail(w.id)) }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(workoutPalette[w.colorIndex % workoutPalette.size]))
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(w.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        val n = wexCount.value[w.id] ?: 0
                                        Text(
                                            "$n exercícios${if (w.time.isNotBlank()) " · ${w.time}" else ""}",
                                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall, color = tint, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Legend(MaterialTheme.colorScheme.primary, "Concluído")
            Legend(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), "Planejado")
            Legend(MaterialTheme.colorScheme.error.copy(alpha = 0.8f), "Não realizado")
            Legend(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), "Descanso")
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun Legend(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FrequencyCalendar(workouts: List<Workout>, sessions: List<Session>) {
    var year by remember { mutableStateOf(LocalDate.now().year) }
    var month by remember { mutableStateOf(LocalDate.now().monthValue) }
    val today = DateUtils.today()
    val status = remember(year, month, sessions, workouts) {
        Stats.monthDayStatus(year, month, sessions, workouts)
    }
    val trainedCount = status.count { it.second == 'T' }
    val missedCount = status.count { it.second == 'F' }
    val restCount = status.count { it.second == 'D' }
    val firstDow = DateUtils.dowIso(java.time.LocalDate.of(year, month, 1).toEpochDay())

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                val m = month - 1; if (m < 1) { month = 12; year-- } else month = m
            }) { Icon(Icons.Filled.ChevronLeft, "Mês anterior") }
            Text(DateUtils.fmtMonth(year, month), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            IconButton(onClick = {
                val m = month + 1; if (m > 12) { month = 1; year++ } else month = m
            }) { Icon(Icons.Filled.ChevronRight, "Próximo mês") }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..7).forEach { iso -> Text(DateUtils.dowShort(iso), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        val leading = firstDow - 1
        val cells: List<Long?> = List(leading) { null } + status.map { it.first }
        val rows = cells.chunked(7)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { day ->
                        Box(
                            Modifier.weight(1f).aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                val st = status.first { it.first == day }.second
                                val bg = when (st) {
                                    'T' -> MaterialTheme.colorScheme.primary
                                    'F' -> MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
                                    'P' -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                }
                                Box(
                                    Modifier.fillMaxSize().clip(CircleShape).background(bg).clickable {},
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${LocalDate.ofEpochDay(day).dayOfMonth}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (st == 'T') MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (day == today) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Legend(MaterialTheme.colorScheme.primary, "Treinou")
            Legend(MaterialTheme.colorScheme.error.copy(alpha = 0.75f), "Falta")
            Legend(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), "Planejado")
            Legend(MaterialTheme.colorScheme.surfaceVariant, "Descanso")
        }

        AppCard(Modifier.fillMaxWidth()) {
            Text("${DateUtils.fmtMonth(year, month).uppercase()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCell("$trainedCount", "treinos")
                StatCell("$missedCount", "faltas")
                StatCell("$restCount", "descansos")
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}
