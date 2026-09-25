package com.gymtrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Progresso", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { nav.navigate(Routes.SEARCH) }) { Icon(Icons.Filled.Search, "Buscar") }
        }

        // ---------- totais ----------
        AppCard(Modifier.fillMaxWidth()) {
            Text("ESTATÍSTICAS GERAIS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCell("${totals.sessions}", "treinos")
                StatCell(DateUtils.fmtDuration(totals.totalMinutes), "tempo total")
                StatCell(DateUtils.fmtDuration(totals.avgMinutes), "média/treino")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCell("${totals.exercises}", "exercícios")
                StatCell("${totals.sets}", "séries")
                StatCell("${totals.reps}", "repetições")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCell(
                    if (totals.volume > 0) "${com.gymtrack.app.data.Disp.fmtKg(if (settings.unit == "lb") totals.volume * Disp.LB_PER_KG else totals.volume)} ${settings.unit}" else "0",
                    "volume total"
                )
                StatCell("${totals.weeklyFrequency}x", "freq. semanal")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCell(totals.maxLoad, "maior carga")
                StatCell(totals.bestSession, "melhor treino (volume)")
            }
        }

        // ---------- gráficos ----------
        val wv = remember(tick) { Stats.weeklyVolume(logs, 8) }
        val ws = remember(tick) { Stats.weeklySessions(sessions, 8) }
        AppCard(Modifier.fillMaxWidth()) {
            Text("VOLUME POR SEMANA (kg)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            BarChart(wv)
        }
        AppCard(Modifier.fillMaxWidth()) {
            Text("TREINOS POR SEMANA", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(12.dp))
            BarChart(ws.map { (l, v) -> l to v.toDouble() }, MaterialTheme.colorScheme.secondary, valueFmt = { it.toInt().toString() })
        }

        // ---------- inteligentes ----------
        val insights = remember(tick) { Stats.insights(emptyList(), sessions, logs) }
        if (insights.isNotEmpty()) {
            AppCard(Modifier.fillMaxWidth()) {
                Text("SISTEMA INTELIGENTE", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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

        // ---------- atalhos ----------
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppCard(Modifier.weight(1f), onClick = { nav.navigate(Routes.RECORDS) }) {
                Icon(Icons.Filled.EmojiEvents, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(6.dp))
                Text("Recordes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            AppCard(Modifier.weight(1f), onClick = { nav.navigate(Routes.MEASUREMENTS) }) {
                Icon(Icons.Filled.Straighten, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(6.dp))
                Text("Medidas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            AppCard(Modifier.weight(1f), onClick = { nav.navigate(Routes.PHOTOS) }) {
                Icon(Icons.Filled.PhotoCamera, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(6.dp))
                Text("Fotos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
        }

        // ---------- evolução por exercício ----------
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
                                    (if (diff > 0) "  (+" + com.gymtrack.app.data.Disp.fmtKg(diff) + " kg desde o início)" else if (diff < 0) " (${com.gymtrack.app.data.Disp.fmtKg(diff)} kg)" else ""),
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
