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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.*
import com.gymtrack.app.ui.nav.Routes

@Composable
fun ExerciseCatalogScreen(nav: NavHostController) {
    val tick = refreshTick()
    var all by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var group by remember { mutableStateOf<String?>(null) }
    var showCustom by remember { mutableStateOf(false) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { all = Repo.exercises() }
    }

    val filtered = all
        .filter { group == null || it.muscleGroup == group }
        .filter { query.isBlank() || it.name.contains(query, true) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
                Text("Banco de exercícios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LabeledTextField(query, { query = it }, "🔍 Buscar exercício...")
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(selected = group == null, onClick = { group = null }, label = { Text("Todos") })
                Seed.MUSCLE_GROUPS.forEach { g ->
                    FilterChip(selected = group == g, onClick = { group = if (group == g) null else g }, label = { Text(g) })
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 90.dp)) {
                items(filtered.size) { i ->
                    val e = filtered[i]
                    AppCard(Modifier.fillMaxWidth(), onClick = { nav.navigate(Routes.exerciseDetail(e.id)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(e.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f, fill = false))
                                    if (e.isCustom) AssistMini("personalizado")
                                }
                                Text(
                                    listOfNotNull(e.muscleGroup, e.equipment.ifBlank { null }).joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                if (e.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                "Favorito",
                                tint = if (e.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { showCustom = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(6.dp))
            Text("Personalizado")
        }
    }

    if (showCustom) {
        var name by remember { mutableStateOf("") }
        var groupPick by remember { mutableStateOf(false) }
        var customGroup by remember { mutableStateOf(Seed.MUSCLE_GROUPS.first()) }
        AlertDialog(
            onDismissRequest = { showCustom = false },
            title = { Text("Criar exercício personalizado", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabeledTextField(name, { name = it.take(60) }, "Nome do exercício")
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .clickable { groupPick = true }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grupo muscular", modifier = Modifier.weight(1f))
                        Text(customGroup, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        Graph.launch {
                            val id = Repo.insertExercise(Exercise(name = name.trim(), muscleGroup = customGroup, isCustom = true))
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                showCustom = false
                                nav.navigate(Routes.exerciseDetail(id))
                            }
                        }
                    }
                }) { Text("Criar e editar") }
            },
            dismissButton = { TextButton(onClick = { showCustom = false }) { Text("Cancelar") } }
        )
        if (groupPick) {
            RadioPickerDialog("Grupo muscular", Seed.MUSCLE_GROUPS, customGroup, onSelect = { customGroup = it; groupPick = false }, onDismiss = { groupPick = false })
        }
    }
}

@Composable
fun ExerciseDetailScreen(nav: NavHostController, exerciseId: Long) {
    val tick = refreshTick()
    val settings = LocalAppSettings.current
    var ex by remember { mutableStateOf<Exercise?>(null) }
    var history by remember { mutableStateOf<List<SetLog>>(emptyList()) }
    var noteEdit by remember { mutableStateOf(false) }
    var fieldEdit by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(exerciseId, tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            ex = Repo.exercise(exerciseId)
            history = Repo.exerciseHistory(exerciseId)
        }
    }

    val e = ex
    if (e == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Exercício", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { Graph.launch { Repo.toggleExerciseFavorite(e) } }) {
                Icon(
                    if (e.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    "Favorito",
                    tint = if (e.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (e.isCustom) {
                IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, "Excluir", tint = MaterialTheme.colorScheme.error) }
            }
        }
        Text(e.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistMini(e.muscleGroup)
            if (e.equipment.isNotBlank()) AssistMini(e.equipment)
            if (e.isCustom) AssistMini("personalizado")
        }
        Spacer(Modifier.height(16.dp))

        if (e.secondary.isNotBlank()) InfoCard("Músculos secundários", e.secondary)
        if (e.instructions.isNotBlank()) InfoCard("Como executar", e.instructions)
        if (e.tips.isNotBlank()) InfoCard("Dicas de execução", e.tips)
        if (e.mistakes.isNotBlank()) InfoCard("Erros comuns", e.mistakes)

        AppCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📝 Observação pessoal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                TextButton(onClick = { noteEdit = true }) { Text(if (e.personalNote.isBlank()) "Adicionar" else "Editar") }
            }
            if (e.personalNote.isNotBlank()) {
                Text(e.personalNote, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (e.isCustom) {
            Spacer(Modifier.height(6.dp))
            TonalButton2("EDITAR INFORMAÇÕES", { fieldEdit = true }, Modifier.fillMaxWidth(), Icons.Filled.Edit)
        }

        Spacer(Modifier.height(18.dp))
        SectionTitle("Meu progresso neste exercício")
        Spacer(Modifier.height(8.dp))
        if (history.isEmpty()) {
            AppCard(Modifier.fillMaxWidth()) { EmptyState("Sem registros ainda. Faça o treino para começar a acompanhar a evolução.", Icons.Filled.TrendingUp) }
        } else {
            val best = history.maxByOrNull { it.weight }
            AppCard(Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    StatCell(Disp.weight(best?.weight ?: 0.0, settings.unit, withUnit = false), "melhor carga")
                    StatCell("${history.size}", "séries feitas")
                    StatCell("${history.sumOf { it.reps }}", "repetições")
                }
            }
            Spacer(Modifier.height(10.dp))
            val byDay = history.groupBy { DateUtils.epochDayOfMillis(it.timestamp) }.toSortedMap()
            val chartData = byDay.map { (day, logs) ->
                val d = logs.maxOf { it.weight }
                day to d
            }.takeLast(12)
            if (chartData.size >= 2) {
                AppCard(Modifier.fillMaxWidth()) {
                    Text("Evolução de carga", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    LineChart(chartData.map { (d, v) -> DateUtils.fmtDate(d) to v })
                }
                Spacer(Modifier.height(10.dp))
            }
            AppCard(Modifier.fillMaxWidth()) {
                Text("Histórico", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                byDay.entries.toList().asReversed().take(10).forEach { (day, logs) ->
                    val bestLog = logs.maxByOrNull { it.weight }
                    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(DateUtils.fmtDate(day), style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(90.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${logs.size} séries · melhor: ${bestLog?.reps}×${Disp.weight(bestLog?.weight ?: 0.0, settings.unit)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }

    if (noteEdit) {
        var txt by remember { mutableStateOf(e.personalNote) }
        AlertDialog(
            onDismissRequest = { noteEdit = false },
            title = { Text("Observação pessoal", style = MaterialTheme.typography.titleMedium) },
            text = { LabeledTextField(txt, { txt = it.take(400) }, "Ex.: máquina ocupada, ajuste do banco...", minLines = 3, singleLine = false) },
            confirmButton = {
                Button(onClick = {
                    Graph.launch {
                        Repo.updateExercise(e.copy(personalNote = txt))
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { noteEdit = false }
                    }
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { noteEdit = false }) { Text("Cancelar") } }
        )
    }

    if (fieldEdit) {
        var name by remember { mutableStateOf(e.name) }
        var groupPick by remember { mutableStateOf(false) }
        var group by remember { mutableStateOf(e.muscleGroup.ifBlank { Seed.MUSCLE_GROUPS.first() }) }
        var equipment by remember { mutableStateOf(e.equipment) }
        var instructions by remember { mutableStateOf(e.instructions) }
        AlertDialog(
            onDismissRequest = { fieldEdit = false },
            title = { Text("Editar exercício", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabeledTextField(name, { name = it.take(60) }, "Nome")
                    Row(Modifier.fillMaxWidth().clickable { groupPick = true }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Grupo muscular", modifier = Modifier.weight(1f))
                        Text(group, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    LabeledTextField(equipment, { equipment = it.take(60) }, "Equipamento")
                    LabeledTextField(instructions, { instructions = it.take(600) }, "Instruções", minLines = 3, singleLine = false)
                }
            },
            confirmButton = {
                Button(onClick = {
                    Graph.launch {
                        Repo.updateExercise(e.copy(name = name.ifBlank { e.name }, muscleGroup = group, equipment = equipment, instructions = instructions))
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { fieldEdit = false }
                    }
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { fieldEdit = false }) { Text("Cancelar") } }
        )
        if (groupPick) {
            RadioPickerDialog("Grupo muscular", Seed.MUSCLE_GROUPS, group, onSelect = { group = it; groupPick = false }, onDismiss = { groupPick = false })
        }
    }

    if (confirmDelete) {
        ConfirmDialog("Excluir exercício?", "\"${e.name}\" será removido do banco. Treinos e histórico mantêm o nome salvo.", confirmText = "Excluir",
            onConfirm = {
                Graph.launch {
                    Repo.deleteExercise(e.id)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { nav.popBackStack() }
                }
            }, onDismiss = { confirmDelete = false })
    }
}

@Composable
private fun InfoCard(title: String, content: String) {
    AppCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(content, style = MaterialTheme.typography.bodyMedium)
    }
}
