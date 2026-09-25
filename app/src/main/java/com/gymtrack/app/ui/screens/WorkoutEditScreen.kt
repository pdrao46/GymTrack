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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.*
import com.gymtrack.app.ui.nav.Routes

@Composable
fun WorkoutEditScreen(nav: NavHostController, workoutId: Long) {
    val settings = LocalAppSettings.current
    val isNew = workoutId <= 0
    val tick = refreshTick()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var day by remember { mutableStateOf<Int?>(null) }
    var time by remember { mutableStateOf("") }
    var estMinutes by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }
    var wex by remember { mutableStateOf(listOf<WorkoutExercise>()) }
    var loaded by remember { mutableStateOf(!isNew) }
    var saving by remember { mutableStateOf(false) }

    var pickingExercise by remember { mutableStateOf(false) }
    var editingEx by remember { mutableStateOf<WorkoutExercise?>(null) }

    LaunchedEffect(tick) {
        if (!isNew && !loaded) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val w = Repo.workout(workoutId)
                if (w != null) {
                    name = w.name; description = w.description; day = w.dayOfWeek
                    time = w.time; estMinutes = if (w.estimatedMinutes > 0) w.estimatedMinutes.toString() else ""
                    notes = w.notes; colorIndex = w.colorIndex
                    wex = Repo.workoutExercises(workoutId)
                }
                loaded = true
            }
        }
    }

    fun autoEstimate(): Int {
        val total = wex.sumOf { it.sets * (it.restSeconds + 45) }
        return (total + 59) / 60
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text(if (isNew) "Novo treino" else "Editar treino", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))

        LabeledTextField(name, { name = it.take(60) }, "Nome do treino (ex.: TREINO A — PEITO + TRÍCEPS)")
        Spacer(Modifier.height(10.dp))
        LabeledTextField(description, { description = it.take(200) }, "Descrição", minLines = 2, singleLine = false)
        Spacer(Modifier.height(14.dp))

        Text("Dia da semana", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = day == null, onClick = { day = null }, label = { Text("Livre") }, modifier = Modifier.weight(1.2f))
            (1..7).forEach { iso ->
                FilterChip(
                    selected = day == iso,
                    onClick = { day = iso },
                    label = { Text(DateUtils.dowShort(iso)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TimeField(time, { time = it }, Modifier.weight(1f))
            LabeledTextField(estMinutes, { estMinutes = it.filter { c -> c.isDigit() }.take(3) }, "Tempo estimado (min)", Modifier.weight(1f), KeyboardType.Number)
        }
        TextButton(onClick = { estMinutes = autoEstimate().toString() }) {
            Icon(Icons.Filled.Calculate, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Calcular tempo estimado automaticamente")
        }

        Spacer(Modifier.height(6.dp))
        Text("Cor do treino", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            workoutPalette.forEachIndexed { i, c ->
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(c)
                        .clickable { colorIndex = i },
                    contentAlignment = Alignment.Center
                ) {
                    if (colorIndex == i) Icon(Icons.Filled.Check, null, tint = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        LabeledTextField(notes, { notes = it.take(400) }, "Observações gerais do treino", minLines = 2, singleLine = false)

        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Exercícios (${wex.size})")
            Spacer(Modifier.weight(1f))
            Button(onClick = { pickingExercise = true }, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("ADICIONAR EXERCÍCIO")
            }
        }
        Spacer(Modifier.height(8.dp))

        wex.forEachIndexed { i, e ->
            AppCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${i + 1}. ${e.exerciseName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${e.sets}×${if (e.repsMin == e.repsMax) "${e.repsMin}" else "${e.repsMin}–${e.repsMax}"}" +
                                (if (e.weight > 0) " · " + Disp.weight(e.weight, settings.unit) else "") +
                                " · ${e.restSeconds}s descanso",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        if (i > 0) wex = wex.toMutableList().apply { add(i - 1, removeAt(i)) }
                    }) { Icon(Icons.Filled.KeyboardArrowUp, "Subir") }
                    IconButton(onClick = {
                        if (i < wex.size - 1) wex = wex.toMutableList().apply { add(i + 1, removeAt(i)) }
                    }) { Icon(Icons.Filled.KeyboardArrowDown, "Descer") }
                    IconButton(onClick = { editingEx = e }) { Icon(Icons.Filled.Edit, "Editar") }
                    IconButton(onClick = { wex = wex.toMutableList().apply { removeAt(i) } }) {
                        Icon(Icons.Filled.Close, "Remover", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        PrimaryBigButton("SALVAR TREINO", {
            if (name.isBlank()) {
                name = "Treino ${day?.let { DateUtils.dowLong(it) } ?: ""}".trim()
            }
            saving = true
            Graph.launch {
                val id = if (isNew) {
                    val n = Repo.workouts().size
                    val wid = Repo.insertWorkout(
                        Workout(
                            name = name, description = description, dayOfWeek = day, time = time,
                            estimatedMinutes = estMinutes.toIntOrNull() ?: autoEstimate(),
                            notes = notes, colorIndex = colorIndex
                        )
                    )
                    wid
                } else {
                    Repo.updateWorkout(
                        Workout(
                            id = workoutId, name = name, description = description, dayOfWeek = day, time = time,
                            estimatedMinutes = estMinutes.toIntOrNull() ?: autoEstimate(),
                            notes = notes, colorIndex = colorIndex, createdAt = Repo.workout(workoutId)?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                    workoutId
                }
                Repo.saveWorkoutExercises(id, wex.mapIndexed { i, e -> e.copy(orderIndex = i) })
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { nav.popBackStack() }
            }
        }, icon = Icons.Filled.Save, enabled = !saving)
        Spacer(Modifier.height(30.dp))
    }

    // ---------- seleção de exercício ----------
    if (pickingExercise) {
        ExercisePickerDialog(
            onPick = { ex ->
                pickingExercise = false
                editingEx = WorkoutExercise(
                    workoutId = if (isNew) 0 else workoutId,
                    exerciseId = ex.id, exerciseName = ex.name, muscleGroup = ex.muscleGroup
                )
            },
            onDismiss = { pickingExercise = false }
        )
    }

    editingEx?.let { target ->
        ExConfigDialog(
            initial = ExConfig(
                sets = target.sets, repsMin = target.repsMin, repsMax = target.repsMax,
                weight = target.weight, restSeconds = target.restSeconds,
                rir = target.rir, rpe = target.rpe, tempo = target.tempo,
                method = target.method, notes = target.notes
            ),
            unit = settings.unit,
            title = target.exerciseName,
            onConfirm = { cfg ->
                val updated = target.copy(
                    sets = cfg.sets, repsMin = cfg.repsMin, repsMax = cfg.repsMax,
                    weight = cfg.weight, restSeconds = cfg.restSeconds,
                    rir = cfg.rir, rpe = cfg.rpe, tempo = cfg.tempo,
                    method = cfg.method, notes = cfg.notes
                )
                val idx = wex.indexOfFirst { it === target || it.id == target.id }
                if (idx >= 0) wex = wex.toMutableList().apply { this[idx] = updated } else wex = wex + updated
                editingEx = null
            },
            onDismiss = { editingEx = null }
        )
    }
}

// ---------- diálogo para escolher exercício do catálogo ----------

@Composable
fun ExercisePickerDialog(
    onPick: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var all by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var showCustom by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customGroup by remember { mutableStateOf(Seed.MUSCLE_GROUPS.first()) }
    var groupPick by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { all = Repo.exercises() }
    }

    val filtered = if (query.isBlank()) all else all.filter {
        it.name.contains(query, true) || it.muscleGroup.contains(query, true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar exercício", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                LabeledTextField(query, { query = it }, "Buscar exercício...")
                Spacer(Modifier.height(8.dp))
                Column(Modifier.height(320.dp).verticalScroll(rememberScrollState())) {
                    if (filtered.isEmpty()) {
                        Text("Nada encontrado. Crie um exercício personalizado abaixo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    filtered.forEach { ex ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .clickable { onPick(ex) }.padding(vertical = 9.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.FitnessCenter, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(ex.name, style = MaterialTheme.typography.bodyMedium)
                                Text(ex.muscleGroup, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = { showCustom = true }) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                    Text("CRIAR EXERCÍCIO PERSONALIZADO")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )

    if (showCustom) {
        AlertDialog(
            onDismissRequest = { showCustom = false },
            title = { Text("Exercício personalizado", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabeledTextField(customName, { customName = it.take(60) }, "Nome do exercício")
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .clickable { groupPick = true }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grupo muscular", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text(customGroup, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.ChevronRight, null, modifier = Modifier.size(16.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (customName.isNotBlank()) {
                        Graph.launch {
                            val id = Repo.insertExercise(
                                Exercise(name = customName.trim(), muscleGroup = customGroup, isCustom = true)
                            )
                            val ex = Repo.exercise(id)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                showCustom = false
                                if (ex != null) onPick(ex)
                            }
                        }
                    }
                }) { Text("Criar") }
            },
            dismissButton = { TextButton(onClick = { showCustom = false }) { Text("Cancelar") } }
        )
    }

    if (groupPick) {
        RadioPickerDialog(
            "Grupo muscular",
            Seed.MUSCLE_GROUPS,
            customGroup,
            onSelect = { customGroup = it; groupPick = false },
            onDismiss = { groupPick = false }
        )
    }
}
