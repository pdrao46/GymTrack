package com.gymtrack.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import java.time.LocalDate

// ================= RECORDES =================

@Composable
fun RecordsScreen(nav: NavHostController) {
    val tick = refreshTick()
    val settings = LocalAppSettings.current
    var records by remember { mutableStateOf<List<PersonalRecord>>(emptyList()) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            records = Stats.personalRecords(Repo.allLogs())
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Meus recordes 🏆", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text(
            "Os recordes são registrados automaticamente com base nas séries concluídas.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(Modifier.height(12.dp))
        if (records.isEmpty()) {
            AppCard(Modifier.fillMaxWidth()) {
                EmptyState("Nenhum recorde ainda. Registre cargas nos treinos para começar a competir com você mesmo!", Icons.Filled.EmojiEvents)
            }
        }
        records.forEach { pr ->
            AppCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(pr.exerciseName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Melhor carga: ${Disp.weight(pr.maxWeight, settings.unit)} × ${pr.reps} reps" +
                                (if (pr.muscleGroup.isNotBlank()) " · ${pr.muscleGroup}" else ""),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        DateUtils.fmtDate(pr.dateEpochDay),
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ================= MEDIDAS =================

@Composable
fun MeasurementsScreen(nav: NavHostController) {
    val tick = refreshTick()
    val settings = LocalAppSettings.current

    var list by remember { mutableStateOf<List<Measurement>>(emptyList()) }

    // formulário
    var dateTxt by remember { mutableStateOf(DateUtils.fmtDate(DateUtils.today())) }
    var weightTxt by remember { mutableStateOf("") }
    var arm by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hip by remember { mutableStateOf("") }
    var thigh by remember { mutableStateOf("") }
    var calf by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showChart by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { list = Repo.measurements() }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Medidas corporais", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))

        AppCard(Modifier.fillMaxWidth()) {
            Text("Nova medição", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            LabeledTextField(dateTxt, { dateTxt = it.take(10) }, "Data (dd/mm/aaaa)")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledTextField(weightTxt, { weightTxt = num(it) }, if (settings.unit == "lb") "Peso (lb)" else "Peso (kg)", Modifier.weight(1f), DecimalKeyboard())
                LabeledTextField(arm, { arm = num(it) }, "Braço (cm)", Modifier.weight(1f), DecimalKeyboard())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledTextField(chest, { chest = num(it) }, "Peito (cm)", Modifier.weight(1f), DecimalKeyboard())
                LabeledTextField(waist, { waist = num(it) }, "Cintura (cm)", Modifier.weight(1f), DecimalKeyboard())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledTextField(hip, { hip = num(it) }, "Quadril (cm)", Modifier.weight(1f), DecimalKeyboard())
                LabeledTextField(thigh, { thigh = num(it) }, "Coxa (cm)", Modifier.weight(1f), DecimalKeyboard())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledTextField(calf, { calf = num(it) }, "Panturrilha (cm)", Modifier.weight(1f), DecimalKeyboard())
            }
            LabeledTextField(note, { note = it.take(200) }, "Observação")
            Spacer(Modifier.height(10.dp))
            PrimaryBigButton("SALVAR MEDIÇÃO", {
                val day = parseDate(dateTxt) ?: DateUtils.today()
                Graph.launch {
                    Repo.insertMeasurement(
                        Measurement(
                            dateEpochDay = day,
                            weight = if (weightTxt.isBlank()) -1.0 else Disp.toKg(weightTxt.toDoubleOrNull() ?: -1.0, settings.unit),
                            arm = arm.toDoubleOrNull() ?: -1.0,
                            chest = chest.toDoubleOrNull() ?: -1.0,
                            waist = waist.toDoubleOrNull() ?: -1.0,
                            hip = hip.toDoubleOrNull() ?: -1.0,
                            thigh = thigh.toDoubleOrNull() ?: -1.0,
                            calf = calf.toDoubleOrNull() ?: -1.0,
                            note = note
                        )
                    )
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        weightTxt = ""; arm = ""; chest = ""; waist = ""; hip = ""; thigh = ""; calf = ""; note = ""
                    }
                }
            }, icon = Icons.Filled.Save)
        }

        Spacer(Modifier.height(16.dp))
        if (list.isNotEmpty()) {
            SectionTitle("Evolução")
            Spacer(Modifier.height(8.dp))
            val chartTargets = listOf("weight" to "Peso", "arm" to "Braço", "chest" to "Peito", "waist" to "Cintura", "hip" to "Quadril", "thigh" to "Coxa", "calf" to "Panturrilha")
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                chartTargets.forEach { (key, label) ->
                    FilterChip(selected = showChart == key, onClick = { showChart = key }, label = { Text(label) })
                }
            }
            Spacer(Modifier.height(10.dp))
            showChart?.let { key ->
                val asc = list.sortedBy { it.dateEpochDay }
                val data: List<Pair<Long, Double>> = asc.mapNotNull { m ->
                    val v = when (key) {
                        "weight" -> m.weight; "arm" -> m.arm; "chest" -> m.chest; "waist" -> m.waist
                        "hip" -> m.hip; "thigh" -> m.thigh; else -> m.calf
                    }
                    if (v > 0) m.dateEpochDay to v else null
                }
                val display: List<Pair<String, Double>> = data.map { (d, v) ->
                    DateUtils.fmtDate(d) to (if (key == "weight" && settings.unit == "lb") v * Disp.LB_PER_KG else v)
                }
                AppCard(Modifier.fillMaxWidth()) {
                    if (display.size >= 2) LineChart(display)
                    else EmptyState("Registre este dado em pelo menos 2 dias para ver o gráfico.", Icons.Filled.ShowChart)
                }
                Spacer(Modifier.height(10.dp))
            }

            SectionTitle("Histórico de medidas")
            Spacer(Modifier.height(8.dp))
            list.forEach { m ->
                AppCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(DateUtils.fmtDate(m.dateEpochDay), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            val parts = mutableListOf<String>()
                            if (m.weight > 0) parts.add("Peso: " + Disp.weight(m.weight, settings.unit))
                            if (m.arm > 0) parts.add("Braço: ${Disp.fmtKg(m.arm)} cm")
                            if (m.chest > 0) parts.add("Peito: ${Disp.fmtKg(m.chest)} cm")
                            if (m.waist > 0) parts.add("Cintura: ${Disp.fmtKg(m.waist)} cm")
                            if (m.hip > 0) parts.add("Quadril: ${Disp.fmtKg(m.hip)} cm")
                            if (m.thigh > 0) parts.add("Coxa: ${Disp.fmtKg(m.thigh)} cm")
                            if (m.calf > 0) parts.add("Panturrilha: ${Disp.fmtKg(m.calf)} cm")
                            if (parts.isEmpty()) parts.add("Sem valores registrados")
                            Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (m.note.isNotBlank()) Text("📝 ${m.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { Graph.launch { Repo.deleteMeasurement(m.id) } }) {
                            Icon(Icons.Filled.Delete, "Excluir", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        } else {
            AppCard(Modifier.fillMaxWidth()) {
                EmptyState("Nenhuma medição registrada. Anote seu peso e medidas para acompanhar a evolução!", Icons.Filled.Straighten)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

private fun num(s: String): String = s.replace(',', '.').filter { it.isDigit() || it == '.' }.take(7)

private fun DecimalKeyboard() = androidx.compose.ui.text.input.KeyboardType.Decimal

private fun parseDate(s: String): Long? {
    val p = s.split("/")
    if (p.size != 3) return null
    val d = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    val y = p[2].toIntOrNull() ?: return null
    return try {
        LocalDate.of(y, m, d).toEpochDay()
    } catch (e: Exception) {
        null
    }
}
