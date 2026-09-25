package com.gymtrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.*
import com.gymtrack.app.ui.nav.Routes

@Composable
fun ProgressScreen(nav: NavHostController) {
    val tick = refreshTick()
    val settings = LocalAppSettings.current

    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var logs by remember { mutableStateOf<List<SetLog>>(emptyList()) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            sessions = Repo.sessions(500)
            logs = Repo.allLogs()
        }
    }

    val doneSessions = sessions.filter { it.endMillis != null }
    val doneIds = doneSessions.map { it.id }.toSet()
    val doneLogs = logs.filter { it.completed && doneIds.contains(it.sessionId) }
    val totals = Stats.totals(
        sessions, logs,
        maxWeightDisplay = doneLogs.maxOfOrNull { it.weight }?.let { "${com.gymtrack.app.data.Disp.fmtKg(it)} kg" } ?: "—",
        bestDisplay = doneSessions.maxOfOrNull { Stats.volumeOf(doneLogs.filter { l -> l.sessionId == it.id }) }
            ?.let { "${com.gymtrack.app.data.Disp.fmtKg(it)} kg" } ?: "—"
    )

    // recordes pessoais + delta real (carga atual x carga anterior)
    val records = remember(tick) { Stats.personalRecords(logs) }
    val recordsWithDelta = remember(tick, records) {
        val completedLogs = logs.filter { it.completed && it.weight > 0 }
        records.map { pr ->
            val prev = completedLogs
                .filter { it.exerciseName == pr.exerciseName && it.weight < pr.maxWeight }
                .maxOfOrNull { it.weight }
            pr to (prev?.let { pr.maxWeight - it })
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Progresso", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { nav.navigate(Routes.SEARCH) }) { Icon(Icons.Filled.Search, "Buscar") }
        }

        // ---------- estatísticas gerais ----------
        EnterSection(0) {
            AppCard(Modifier.fillMaxWidth()) {
                Text(
                    "ESTATÍSTICAS GERAIS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1f.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCell("${totals.sessions}", "treinos", Modifier.weight(1f))
                    StatCell(DateUtils.fmtDuration(totals.totalMinutes), "tempo total", Modifier.weight(1f))
                    StatCell(DateUtils.fmtDuration(totals.avgMinutes), "média/treino", Modifier.weight(1f))
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCell("${totals.exercises}", "exercícios", Modifier.weight(1f))
                    StatCell("${totals.sets}", "séries", Modifier.weight(1f))
                    StatCell("${totals.reps}", "repetições", Modifier.weight(1f))
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCell(
                        if (totals.volume > 0) "${com.gymtrack.app.data.Disp.fmtKg(if (settings.unit == "lb") totals.volume * Disp.LB_PER_KG else totals.volume)} ${settings.unit}" else "0",
                        "volume total",
                        Modifier.weight(1f)
                    )
                    StatCell("${totals.weeklyFrequency}x", "freq. semanal", Modifier.weight(1f))
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCell(totals.maxLoad, "maior carga", Modifier.weight(1f))
                    StatCell(totals.bestSession, "melhor treino", Modifier.weight(1f))
                }
            }
        }

        // ---------- volume ----------
        EnterSection(1) {
            val wv = remember(tick) { Stats.weeklyVolume(logs, 8) }
            AppCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "VOLUME POR SEMANA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1f.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Text("kg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(12.dp))
                BarChart(wv)
            }
        }

        // ---------- frequência ----------
        EnterSection(2) {
            val ws = remember(tick) { Stats.weeklySessions(sessions, 8) }
            AppCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "TREINOS POR SEMANA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1f.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                BarChart(ws.map { (l, v) -> l to v.toDouble() }, MaterialTheme.colorScheme.secondary, valueFmt = { it.toInt().toString() })
            }
        }

        // ---------- cargas por exercício ----------
        if (records.isNotEmpty()) {
            EnterSection(3) {
                AppCard(Modifier.fillMaxWidth()) {
                    Text(
                        "CARGAS POR EXERCÍCIO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1f.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    BarChart(
                        records.take(6).map { pr ->
                            pr.exerciseName.split(" ").first().take(10) to pr.maxWeight
                        }
                    )
                }
            }
        }

        // ---------- recordes ----------
        if (recordsWithDelta.isNotEmpty()) {
            EnterSection(4) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("Recordes", Modifier.weight(1f))
                    TextButton(onClick = { nav.navigate(Routes.RECORDS) }) {
                        Text("Ver todos", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                recordsWithDelta.take(3).forEach { (pr, delta) ->
                    AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.RECORDS) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(38.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) { Text("🏆", style = MaterialTheme.typography.titleMedium) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(pr.exerciseName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "${Disp.weight(pr.maxWeight, settings.unit)} × ${pr.reps} reps",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (delta != null && delta > 0) {
                                PillBadge(
                                    "+${com.gymtrack.app.data.Disp.fmtKg(delta)} kg",
                                    container = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    content = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                PillBadge(DateUtils.fmtDate(pr.dateEpochDay))
                            }
                        }
                    }
                }
            }
        }

        // ---------- sistema inteligente ----------
        val insights = remember(tick) { Stats.insights(emptyList(), sessions, logs) }
        if (insights.isNotEmpty()) {
            EnterSection(5) {
                AppCard(Modifier.fillMaxWidth()) {
                    Text(
                        "SISTEMA INTELIGENTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1f.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    insights.forEach { txt ->
                        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                            Text("💡")
                            Spacer(Modifier.width(8.dp))
                            Text(txt, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // ---------- atalhos ----------
        EnterSection(6) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppCard(Modifier.weight(1f), onClick = { nav.navigate(Routes.RECORDS) }) {
                    Icon(Icons.Filled.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Recordes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                AppCard(Modifier.weight(1f), onClick = { nav.navigate(Routes.MEASUREMENTS) }) {
                    Icon(Icons.Filled.Straighten, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Medidas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                AppCard(Modifier.weight(1f), onClick = { nav.navigate(Routes.PHOTOS) }) {
                    Icon(Icons.Filled.PhotoCamera, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Fotos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // ---------- evolução por exercício ----------
        EnterSection(7) {
            SectionTitle("Evolução por exercício")
            val byEx = doneLogs.groupBy { it.exerciseId to it.exerciseName }
                .filter { (k, _) -> k.first > 0 }
            if (byEx.isEmpty()) {
                AppCard(Modifier.fillMaxWidth()) { EmptyState("Treine e registre séries para ver a evolução de cada exercício aqui.", Icons.Filled.TrendingUp) }
            } else {
                byEx.entries.sortedBy { it.key.second }.take(50).forEach { (key, list) ->
                    AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.exerciseDetail(key.first)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(key.second, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                val first = list.first()
                                val last = list.last()
                                val diff = last.weight - first.weight
                                Text(
                                    "última: ${last.reps} × ${Disp.weight(last.weight, settings.unit)}" +
                                        (if (diff > 0) "  (+${com.gymtrack.app.data.Disp.fmtKg(diff)} kg desde o início)" else if (diff < 0) " (${com.gymtrack.app.data.Disp.fmtKg(diff)} kg)" else ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
