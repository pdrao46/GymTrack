package com.gymtrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.*
import com.gymtrack.app.ui.nav.Routes

@Composable
fun WorkoutsScreen(nav: NavHostController) {
    val tick = refreshTick()
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var counts by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val w = Repo.workouts()
            val c = HashMap<Long, Int>()
            for (x in w) c[x.id] = Repo.workoutExercises(x.id).size
            workouts = w; counts = c
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Treinos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { nav.navigate(Routes.TEMPLATES) }) { Icon(Icons.Filled.AutoAwesome, "Modelos prontos") }
                IconButton(onClick = { nav.navigate(Routes.CATALOG) }) { Icon(Icons.Filled.ListAlt, "Banco de exercícios") }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 90.dp)) {
                if (workouts.isEmpty()) {
                    item {
                        AppCard(Modifier.fillMaxWidth()) {
                            EmptyState(
                                "Nenhum treino criado ainda.\nCrie o seu primeiro treino ou use um modelo pronto!",
                                Icons.Filled.FitnessCenter
                            )
                        }
                    }
                    item {
                        TonalButton2("VER MODELOS PRONTOS", { nav.navigate(Routes.TEMPLATES) }, Modifier.fillMaxWidth(), Icons.Filled.AutoAwesome)
                    }
                }
                items(workouts.size) { i ->
                    val w = workouts[i]
                    val n = counts[w.id] ?: 0
                    AppCard(Modifier.fillMaxWidth(), onClick = { nav.navigate(Routes.workoutDetail(w.id)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(12.dp).clip(CircleShape).background(workoutPalette[w.colorIndex % workoutPalette.size]))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(w.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (w.description.isNotBlank()) {
                                    Text(w.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    AssistMini(if (w.dayOfWeek != null) DateUtils.dowLong(w.dayOfWeek!!) else "Sem dia fixo")
                                    if (w.time.isNotBlank()) AssistMini(w.time)
                                    AssistMini("$n exercícios")
                                }
                            }
                            Icon(
                                if (w.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                "Favorito",
                                tint = if (w.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { nav.navigate(Routes.workoutEdit(-1L)) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(6.dp))
            Text("Novo treino")
        }
    }
}

@Composable
fun AssistMini(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

// ================= Modelos prontos =================

@Composable
fun TemplatesScreen(nav: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var applied by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modelos prontos") },
                navigationIcon = { IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") } }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Modelos são apenas exemplos editáveis para facilitar a criação — não são recomendações médicas ou de treino.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Seed.TEMPLATES.forEach { t ->
                AppCard(Modifier.fillMaxWidth()) {
                    Text(t.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(t.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    t.days.forEach { d ->
                        Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarMonth, null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${if (d.dayOfWeek != null) DateUtils.dowShort(d.dayOfWeek!!) else "Livre"} · ${d.name} (${d.exercises.size} ex.)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    PrimaryBigButton("USAR ESTE MODELO", {
                        Graph.launch {
                            t.days.forEach { d ->
                                val wid = Repo.insertWorkout(
                                    Workout(
                                        name = d.name, description = "Criado a partir do modelo: ${t.title}",
                                        dayOfWeek = d.dayOfWeek, time = d.time,
                                        estimatedMinutes = (d.exercises.sumOf { it.sets * (it.rest + 45) } / 60),
                                        colorIndex = (t.days.indexOf(d)).mod(workoutPalette.size)
                                    )
                                )
                                Repo.saveWorkoutExercises(
                                    wid,
                                    d.exercises.mapIndexed { i, e ->
                                        val cat = Repo.exercises().firstOrNull { it.name == e.name }
                                        WorkoutExercise(
                                            workoutId = wid, exerciseId = cat?.id ?: 0,
                                            exerciseName = e.name, muscleGroup = cat?.muscleGroup ?: "",
                                            orderIndex = i, sets = e.sets, repsMin = e.repsMin,
                                            repsMax = e.repsMax, weight = e.weight, restSeconds = e.rest
                                        )
                                    }
                                )
                            }
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                applied = t.title
                                android.widget.Toast.makeText(context, "Modelo \"${t.title}\" adicionado aos seus treinos! ✅", android.widget.Toast.LENGTH_LONG).show()
                                nav.popBackStack()
                            }
                        }
                    }, icon = Icons.Filled.Download)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
