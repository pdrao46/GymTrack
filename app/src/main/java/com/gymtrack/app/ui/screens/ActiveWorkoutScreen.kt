package com.gymtrack.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.text.input.KeyboardType
import com.gymtrack.app.data.*
import com.gymtrack.app.rem.RestSignal
import com.gymtrack.app.ui.*
import com.gymtrack.app.ui.nav.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Estado de uma série na tela de treino */
data class SetState(
    val dbId: Long = 0,        // 0 = ainda não salva
    val setNumber: Int,
    val reps: Int,
    val weight: Double,
    val done: Boolean,
    val note: String = ""
)

@Composable
fun ActiveWorkoutScreen(nav: NavHostController, sessionId: Long) {
    val settings = LocalAppSettings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tick = refreshTick()

    var session by remember { mutableStateOf<Session?>(null) }
    var wex by remember { mutableStateOf<List<WorkoutExercise>>(emptyList()) }
    var setsByEx by remember { mutableStateOf<Map<Long, List<SetState>>>(emptyMap()) }
    var lastByEx by remember { mutableStateOf<Map<Long, SetLog>>(emptyMap()) }
    var exNotes by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var currentIdx by remember { mutableIntStateOf(0) }
    var initialized by remember { mutableStateOf(false) }

    // cronômetro de descanso
    var restRunning by remember { mutableStateOf(false) }
    var restPaused by remember { mutableStateOf(false) }
    var restSeconds by remember { mutableIntStateOf(0) }
    var restTotal by remember { mutableIntStateOf(0) }
    var restDone by remember { mutableStateOf(false) }
    var elapsedTick by remember { mutableIntStateOf(0) }

    var showFinish by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showSetDialog by remember { mutableStateOf<SetState?>(null) }
    var showRestPicker by remember { mutableStateOf(false) }

    // ---------- carregar sessão ----------
    LaunchedEffect(sessionId, tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val s = Repo.session(sessionId) ?: return@withContext
            session = s
            val wid = s.workoutId
            if (wid != null && wid > 0) {
                val list = Repo.workoutExercises(wid)
                wex = list
                lastByEx = Repo.lastLogsByExercise(wid, s.startMillis)
            }
            val logs = Repo.setLogs(sessionId)
            val map = HashMap<Long, List<SetState>>()
            for (e in wex) {
                val ls = logs.filter { it.exerciseId == e.exerciseId }.sortedBy { it.setNumber }
                map[e.exerciseId] = ls.map { SetState(it.id, it.setNumber, it.reps, it.weight, it.completed, it.note) }
                    .ifEmpty { (1..e.sets).map { n -> SetState(setNumber = n, reps = e.repsMax, weight = e.weight, done = false) } }
            }
            setsByEx = map
            val notes = logs.mapNotNull { l -> if (l.note.isNotBlank()) l.exerciseId to l.note else null }.toMap()
            exNotes = notes
            // posiciona no primeiro exercício incompleto (apenas no primeiro carregamento)
            if (!initialized) {
                initialized = true
                val firstIncomplete = wex.indexOfFirst { e -> map[e.exerciseId]?.any { !it.done } ?: true }
                if (firstIncomplete >= 0) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { currentIdx = firstIncomplete }
                }
            }
        }
    }

    val s = session
    if (s == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val current: WorkoutExercise? = wex.getOrNull(currentIdx)
    val totalSets = setsByEx.values.sumOf { it.size }
    val doneSets = setsByEx.values.sumOf { l -> l.count { it.done } }
    val finished = wex.isNotEmpty() && wex.indices.all { i ->
        setsByEx[wex[i].exerciseId]?.all { it.done } ?: false
    }

    // relógio decorrido
    LaunchedEffect(s.id) {
        while (true) {
            elapsedTick++
            delay(1000)
        }
    }
    val pausedNow = s.pausedAt != null
    val elapsed = Stats.elapsedMillis(s)

    // contagem regressiva do descanso
    LaunchedEffect(restRunning) {
        while (restRunning) {
            if (!restPaused) {
                delay(1000)
                restSeconds--
                if (restSeconds <= 0) {
                    restRunning = false
                    restDone = true
                    RestSignal.play(context)
                }
            } else {
                delay(200)
            }
        }
    }

    // "DESCANSO FINALIZADO" fica alguns segundos e desaparece
    LaunchedEffect(restDone) {
        if (restDone) {
            delay(3200)
            restDone = false
            restSeconds = 0
        }
    }

    fun startRest(sec: Int) {
        restSeconds = sec
        restTotal = sec
        restPaused = false
        restRunning = true
        restDone = false
    }

    fun persistSession(updated: Session) {
        session = updated
        Graph.launch { Repo.updateSession(updated) }
    }

    fun markSet(exercise: WorkoutExercise, st: SetState, done: Boolean) {
        scope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val log = SetLog(
                    id = st.dbId, sessionId = sessionId, exerciseId = exercise.exerciseId,
                    exerciseName = exercise.exerciseName, muscleGroup = exercise.muscleGroup,
                    setNumber = st.setNumber, reps = st.reps, weight = st.weight,
                    rir = exercise.rir, rpe = exercise.rpe,
                    completed = done,
                    note = st.note,
                    timestamp = System.currentTimeMillis()
                )
                val newId = if (st.dbId > 0) { Repo.updateSetLog(log); st.dbId } else Repo.insertSetLog(log)
                val cur = setsByEx[exercise.exerciseId] ?: emptyList()
                setsByEx = setsByEx + (exercise.exerciseId to cur.map {
                    if (it.setNumber == st.setNumber) it.copy(dbId = newId, done = done) else it
                })
                if (done) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (exercise.restSeconds > 0) startRest(exercise.restSeconds)
                        // avança automaticamente quando terminar todas as séries do exercício
                        val list = setsByEx[exercise.exerciseId] ?: emptyList()
                        if (list.all { it.done } && currentIdx < wex.size - 1) {
                            currentIdx++
                        }
                    }
                }
            }
        }
    }

    fun updateSet(exercise: WorkoutExercise, st: SetState) {
        // só atualiza valores (reps/peso) — salva se já existir
        if (st.dbId > 0) {
            Graph.launch {
                Repo.setLogs(sessionId).firstOrNull { it.id == st.dbId }?.let { log ->
                    Repo.updateSetLog(log.copy(reps = st.reps, weight = st.weight, note = st.note))
                }
            }
        }
        val cur = setsByEx[exercise.exerciseId] ?: emptyList()
        setsByEx = setsByEx + (exercise.exerciseId to cur.map {
            if (it.setNumber == st.setNumber) st else it
        })
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(8.dp))
            // ---------- cabeçalho ----------
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { nav.popBackStack() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) { Icon(Icons.Filled.Close, "Sair da tela", modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        s.workoutName.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6f.sp,
                        maxLines = 1
                    )
                    Text(
                        "${doneSets}/$totalSets séries · ${wex.count { e -> setsByEx[e.exerciseId]?.all { it.done } ?: false }}/${wex.size} exercícios",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (pausedNow) {
                    PillBadge(
                        "⏸ PAUSADO",
                        container = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                        content = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    PillBadge(
                        DateUtils.fmtTimeMillis(elapsed),
                        container = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        content = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            FilledProgressBar(if (totalSets > 0) doneSets.toFloat() / totalSets else 0f)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TonalButton2(
                    if (pausedNow) "RETOMAR" else "PAUSAR TREINO",
                    {
                        if (pausedNow) {
                            val extra = System.currentTimeMillis() - (s.pausedAt ?: System.currentTimeMillis())
                            persistSession(s.copy(pausedAt = null, pausedMillis = s.pausedMillis + extra))
                        } else {
                            persistSession(s.copy(pausedAt = System.currentTimeMillis()))
                        }
                    },
                    Modifier.weight(1f),
                    if (pausedNow) Icons.Filled.PlayArrow else Icons.Filled.Pause
                )
                TonalButton2("FINALIZAR", { showFinish = true }, Modifier.weight(1f), Icons.Filled.Stop)
            }

            // ---------- exercício atual ----------
            Spacer(Modifier.height(14.dp))
            if (wex.isEmpty() || current == null) {
                AppCard(Modifier.fillMaxWidth()) {
                    EmptyState("Este treino não tem exercícios. Finalize ou edite o treino.", Icons.Filled.Info)
                }
            } else {
                val curSets = setsByEx[current.exerciseId] ?: emptyList()
                val currentSetNo = curSets.firstOrNull { !it.done }?.setNumber
                val exerciseDone = curSets.isNotEmpty() && curSets.all { it.done }

                // card grande do exercício atual
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (currentIdx > 0) currentIdx-- },
                            enabled = currentIdx > 0,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Filled.ChevronLeft, "Anterior",
                                tint = if (currentIdx > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                        Column(
                            Modifier.weight(1f).padding(horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PillBadge("EXERCÍCIO ${currentIdx + 1} DE ${wex.size}")
                            Spacer(Modifier.height(7.dp))
                            Text(
                                current.exerciseName.uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                            if (current.muscleGroup.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    current.muscleGroup,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(5.dp))
                            Text(
                                buildString {
                                    append("${current.sets} séries × ")
                                    if (current.repsMin == current.repsMax) append("${current.repsMin}") else append("${current.repsMin}–${current.repsMax}")
                                    append(" reps · descanso ${current.restSeconds}s")
                                    if (current.weight > 0) append(" · " + Disp.weight(current.weight, settings.unit))
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        IconButton(
                            onClick = { if (currentIdx < wex.size - 1) currentIdx++ },
                            enabled = currentIdx < wex.size - 1,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Filled.ChevronRight, "Próximo",
                                tint = if (currentIdx < wex.size - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }

                    lastByEx[current.exerciseId]?.let { last ->
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Text(
                                "Última vez: ${last.reps} reps × ${Disp.weight(last.weight, settings.unit)}",
                                Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // aviso de exercício concluído
                AnimatedVisibility(
                    visible = exerciseDone,
                    enter = fadeIn(tween(280)) + expandVertically(tween(320, easing = FastOutSlowInEasing))
                ) {
                    Surface(
                        Modifier.fillMaxWidth().padding(top = 10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "EXERCÍCIO CONCLUÍDO ✓",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "SÉRIES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.9f.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${curSets.count { it.done }}/${curSets.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(4.dp))

                // lista de séries — a série atual aparece destacada
                curSets.forEach { st ->
                    val isCurrent = st.setNumber == currentSetNo && !st.done
                    Surface(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(14.dp))
                            .clickable { showSetDialog = st },
                        color = when {
                            st.done -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                            isCurrent -> MaterialTheme.colorScheme.surfaceContainerHigh
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                                st.done -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            }
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(
                                    when {
                                        st.done -> MaterialTheme.colorScheme.primary
                                        isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                        else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                    }
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (st.done) {
                                    Icon(Icons.Filled.Check, "Concluída", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                                } else {
                                    Text(
                                        st.setNumber.toString().padStart(2, '0'),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${st.reps} reps × ${Disp.weight(st.weight, settings.unit)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (isCurrent) {
                                    Text(
                                        "SÉRIE ATUAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8f.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else if (st.note.isNotBlank()) {
                                    Text(
                                        "📝 ${st.note}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            if (st.done) {
                                PopCheck(tint = MaterialTheme.colorScheme.primary, size = 26.dp)
                            } else {
                                Button(
                                    onClick = { markSet(current, st, true) },
                                    contentPadding = PaddingValues(horizontal = 14.dp),
                                    modifier = Modifier.height(38.dp),
                                    shape = RoundedCornerShape(11.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        contentColor = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text("CONCLUIR", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TonalButton2("+ SÉRIE", {
                        val cur = setsByEx[current.exerciseId] ?: emptyList()
                        val nextNum = (cur.maxOfOrNull { it.setNumber } ?: 0) + 1
                        val lastDone = cur.lastOrNull { it.done } ?: cur.lastOrNull()
                        val ns = SetState(
                            setNumber = nextNum,
                            reps = lastDone?.reps ?: current.repsMax,
                            weight = lastDone?.weight ?: current.weight,
                            done = false
                        )
                        setsByEx = setsByEx + (current.exerciseId to cur + ns)
                    }, Modifier.weight(1f), Icons.Filled.Add)
                    TonalButton2("ALTERAR CARGA", { showWeightDialog = true }, Modifier.weight(1f), Icons.Filled.FitnessCenter)
                    TonalButton2("OBSERVAÇÃO", { showNoteDialog = true }, Modifier.weight(1f), Icons.Filled.EditNote)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TonalButton2("PULAR EXERCÍCIO", {
                        val ex = current!!
                        val cur = setsByEx[ex.exerciseId] ?: emptyList()
                        Graph.launch {
                            for (st in cur.filter { !it.done }) {
                                markSetSilent(sessionId, ex, st)
                            }
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                if (currentIdx < wex.size - 1) currentIdx++ else showFinish = true
                            }
                        }
                    }, Modifier.weight(1f), Icons.Filled.SkipNext)
                    TonalButton2("DESCANSO MANUAL", { showRestPicker = true }, Modifier.weight(1f), Icons.Filled.Timer)
                }
                if (finished) {
                    Spacer(Modifier.height(12.dp))
                    PrimaryBigButton("TREINO CONCLUÍDO 🔥 — FINALIZAR", { showFinish = true }, icon = Icons.Filled.EmojiEvents)
                }
            }

            Spacer(Modifier.height(100.dp))
        }

        // ---------- CARD DE DESCANSO (círculo de progresso) ----------
        if (restRunning || restSeconds > 0 || restDone) {
            Surface(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp)),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 10.dp,
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (restDone) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "DESCANSO FINALIZADO",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.6f.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Hora da próxima série 💪",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RingProgress(
                                ratio = if (restTotal > 0) restSeconds.toFloat() / restTotal else 0f,
                                modifier = Modifier.size(92.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                strokeWidth = 8f
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        DateUtils.fmtClock(restSeconds.toLong().coerceAtLeast(0)),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        if (restPaused) "PAUSADO" else "REST",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1f.sp,
                                        color = if (restPaused) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "DESCANSO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1f.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { restSeconds = (restSeconds - 15).coerceAtLeast(0) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) { Text("-15s", style = MaterialTheme.typography.labelMedium) }
                                    Button(
                                        onClick = { restPaused = !restPaused },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(
                                            if (restPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause, null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            if (restPaused) "Seguir" else "Pausar",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { restSeconds += 15 },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) { Text("+15s", style = MaterialTheme.typography.labelMedium) }
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(30, 45, 60, 90, 120).forEach { p ->
                                        AssistChip(
                                            onClick = { restSeconds = p; restTotal = p; restPaused = false },
                                            label = { Text("${p}s", style = MaterialTheme.typography.labelSmall) },
                                            modifier = Modifier.height(30.dp)
                                        )
                                    }
                                    AssistChip(
                                        onClick = { restRunning = false; restSeconds = 0; restTotal = 0 },
                                        label = { Text("Pular ✕", style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.height(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------- presets de descanso ----------
    if (showRestPicker) {
        AlertDialog(
            onDismissRequest = { showRestPicker = false },
            title = { Text("Descanso", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(30, 45, 60).forEach { p ->
                            Button(onClick = { showRestPicker = false; startRest(p) }, modifier = Modifier.weight(1f)) { Text("${p}s") }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(90, 120).forEach { p ->
                            Button(onClick = { showRestPicker = false; startRest(p) }, modifier = Modifier.weight(1f)) { Text("${p}s") }
                        }
                        OutlinedButton(onClick = {
                            showRestPicker = false
                            startRest(current?.restSeconds ?: 60)
                        }, modifier = Modifier.weight(1f)) { Text("Padrão") }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showRestPicker = false }) { Text("Fechar") } }
        )
    }

    // ---------- editar série ----------
    showSetDialog?.let { st ->
        var repsTxt by remember { mutableStateOf(st.reps.toString()) }
        var weightTxt by remember {
            mutableStateOf(if (st.weight > 0) Disp.fmtKg(Disp.fromKg(st.weight, settings.unit)) else "")
        }
        var noteTxt by remember { mutableStateOf(st.note) }
        AlertDialog(
            onDismissRequest = { showSetDialog = null },
            title = { Text("Série ${st.setNumber} — ${current?.exerciseName ?: ""}", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabeledTextField(repsTxt, { repsTxt = it.filter { c -> c.isDigit() }.take(3) }, "Repetições", Modifier.weight(1f), androidx.compose.ui.text.input.KeyboardType.Number)
                        LabeledTextField(weightTxt, { weightTxt = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' }.take(7) }, "Carga (${settings.unit})", Modifier.weight(1f), androidx.compose.ui.text.input.KeyboardType.Decimal)
                    }
                    LabeledTextField(noteTxt, { noteTxt = it.take(200) }, "Observação da série", minLines = 2, singleLine = false)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updated = st.copy(
                        reps = repsTxt.toIntOrNull() ?: st.reps,
                        weight = if (weightTxt.isBlank()) st.weight else Disp.toKg(weightTxt.toDoubleOrNull() ?: st.weight, settings.unit),
                        note = noteTxt
                    )
                    current?.let { updateSet(it, updated) }
                    showSetDialog = null
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { showSetDialog = null }) { Text("Cancelar") } }
        )
    }

    // ---------- alterar carga ----------
    if (showWeightDialog && current != null) {
        var weightTxt by remember {
            val def = setsByEx[current.exerciseId]?.firstOrNull { !it.done }?.weight ?: current.weight
            mutableStateOf(if (def > 0) Disp.fmtKg(Disp.fromKg(def, settings.unit)) else "")
        }
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text("Alterar carga", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Aplica a todas as séries não concluídas de ${current.exerciseName}.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LabeledTextField(weightTxt, { weightTxt = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' }.take(7) }, "Carga (${settings.unit})", keyboardType = KeyboardTypeDecimal())
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1.0, 2.5, 5.0).forEach { inc ->
                            OutlinedButton(onClick = {
                                val cur = (weightTxt.toDoubleOrNull() ?: 0.0) + inc
                                weightTxt = Disp.fmtKg(cur)
                            }) { Text("+${Disp.fmtKg(inc)}") }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val kg = if (weightTxt.isBlank()) 0.0 else Disp.toKg(weightTxt.toDoubleOrNull() ?: 0.0, settings.unit)
                    val cur = setsByEx[current.exerciseId] ?: emptyList()
                    setsByEx = setsByEx + (current.exerciseId to cur.map { if (!it.done) it.copy(weight = kg) else it })
                    showWeightDialog = false
                }) { Text("Aplicar") }
            },
            dismissButton = { TextButton(onClick = { showWeightDialog = false }) { Text("Cancelar") } }
        )
    }

    // ---------- observação do exercício ----------
    if (showNoteDialog && current != null) {
        var txt by remember { mutableStateOf(exNotes[current.exerciseId] ?: "") }
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Observação — ${current.exerciseName}", style = MaterialTheme.typography.titleMedium) },
            text = {
                LabeledTextField(txt, { txt = it.take(300) }, "Ex.: máquina ocupada, senti mais dificuldade na última série...", minLines = 3, singleLine = false)
            },
            confirmButton = {
                Button(onClick = {
                    exNotes = exNotes + (current.exerciseId to txt)
                    showNoteDialog = false
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { showNoteDialog = false }) { Text("Cancelar") } }
        )
    }

    // ---------- finalizar treino ----------
    if (showFinish) {
        FinishWorkoutDialog(
            session = s,
            incomplete = !finished,
            exNotes = exNotes,
            wex = wex,
            setsByEx = setsByEx,
            onDone = {
                showFinish = false
                android.widget.Toast.makeText(context, "Treino salvo! 🔥", android.widget.Toast.LENGTH_LONG).show()
                nav.popBackStack()
            },
            onDismiss = { showFinish = false }
        )
    }
}

private fun KeyboardTypeDecimal() = androidx.compose.ui.text.input.KeyboardType.Decimal

/** Marca séries como puladas (completed = false) sem mexer no estado da UI */
private suspend fun markSetSilent(sessionId: Long, exercise: WorkoutExercise, st: SetState) {
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val log = SetLog(
            id = st.dbId, sessionId = sessionId, exerciseId = exercise.exerciseId,
            exerciseName = exercise.exerciseName, muscleGroup = exercise.muscleGroup,
            setNumber = st.setNumber, reps = st.reps, weight = st.weight,
            completed = false, note = st.note, timestamp = System.currentTimeMillis()
        )
        if (st.dbId > 0) Repo.updateSetLog(log.copy(completed = false)) else Repo.insertSetLog(log)
    }
}

@Composable
private fun FinishWorkoutDialog(
    session: Session,
    incomplete: Boolean,
    exNotes: Map<Long, String>,
    wex: List<WorkoutExercise>,
    setsByEx: Map<Long, List<SetState>>,
    onDone: () -> Unit,
    onDismiss: () -> Unit
) {
    var feeling by remember { mutableIntStateOf(4) }
    var energy by remember { mutableIntStateOf(3) }
    var motivation by remember { mutableIntStateOf(3) }
    var difficulty by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf(session.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (incomplete) "Finalizar treino?" else "TREINO FINALIZADO ✓", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (incomplete) Text(
                    "Ainda existem séries não concluídas. Elas serão registradas como não realizadas.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error
                )
                Text("Como foi o treino?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("😫", "😕", "🙂", "💪", "🔥").forEachIndexed { i, emoji ->
                        val level = i + 1
                        Box(
                            Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (feeling == level) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .clickable { feeling = level },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
                SliderRow("⚡ Energia", energy) { energy = it }
                SliderRow("🎯 Motivação", motivation) { motivation = it }
                SliderRow("😖 Dificuldade", difficulty) { difficulty = it }
                LabeledTextField(notes, { notes = it.take(500) }, "Observações do treino", minLines = 2, singleLine = false)
            }
        },
        confirmButton = {
            Button(onClick = {
                val mergedNotes = StringBuilder(notes)
                exNotes.filter { it.value.isNotBlank() }.forEach { (exId, note) ->
                    val name = wex.firstOrNull { it.exerciseId == exId }?.exerciseName ?: ""
                    mergedNotes.append("\n").append(name).append(": ").append(note)
                }
                Graph.launch {
                    val s2 = session.copy(
                        endMillis = System.currentTimeMillis(),
                        pausedAt = null,
                        completed = !incomplete,
                        feeling = feeling, energy = energy, motivation = motivation,
                        difficulty = difficulty, notes = mergedNotes.toString().trim()
                    )
                    Repo.updateSession(s2)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onDone() }
                }
            }) { Text("Salvar treino") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Continuar treinando") } }
    )
}

@Composable
private fun SliderRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Column {
        Text("$label: ${listOf("", "Muito baixa", "Baixa", "Normal", "Alta", "Muito alta").getOrElse(value) { "" }}", style = MaterialTheme.typography.labelMedium)
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt().coerceIn(1, 5)) },
            valueRange = 1f..5f,
            steps = 3
        )
    }
}
