package com.gymtrack.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtrack.app.data.DateUtils
import com.gymtrack.app.data.RefreshBus
import com.gymtrack.app.data.Settings

/** Configurações globais disponíveis na composição */
val LocalAppSettings = androidx.compose.runtime.staticCompositionLocalOf { Settings() }

/** Observa o barramento de atualização do banco — recompile a tela quando mudar */
@Composable
fun refreshTick(): Int {
    return RefreshBus.tick.collectAsState().value
}

@Composable
private fun Modifier.androidH(): Modifier = this.horizontalScroll(rememberScrollState())

val workoutPalette = listOf(
    Color(0xFF22C55E), Color(0xFFF97316), Color(0xFF3B82F6),
    Color(0xFFA855F7), Color(0xFFEC4899), Color(0xFF14B8A6)
)

// ---------- Microinterações ----------

// ---------- Cartão padrão ----------

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val pad = if (LocalAppSettings.current.comfortableCards) 16 else 12
    val bg = MaterialTheme.colorScheme.surfaceVariant
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    if (onClick != null) {
        Surface(
            modifier = modifier.clip(shape).clickable { onClick() },
            shape = shape,
            color = bg,
            border = border,
            shadowElevation = 1.dp
        ) {
            Column(Modifier.padding(pad), content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = bg,
            border = border,
            shadowElevation = 1.dp
        ) {
            Column(Modifier.padding(pad), content = content)
        }
    }
}

/** Título de seção: indicador accent + caixa alta com hierarquia editorial */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(modifier.padding(start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(3.dp)
                .height(13.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.9.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Cabeçalho padrão das telas internas (voltar + título + ações) */
@Composable
fun BackHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Voltar") }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        actions()
    }
}

/** Estatística compacta: número em destaque + rótulo pequeno */
@Composable
fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Mini cartão de estatística com ícone accent (dashboard) */
@Composable
fun StatTile(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        shadowElevation = 1.dp
    ) {
        Column(
            Modifier.padding(horizontal = 6.dp, vertical = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = tint)
            Spacer(Modifier.height(5.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(1.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Tag pequena em pílula */
@Composable
fun PillBadge(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    content: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text,
        modifier = modifier
            .background(container, RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = content,
        maxLines = 1
    )
}

/** Barra de progresso com animação suave ao mudar o valor */
@Composable
fun FilledProgressBar(ratio: Float, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    val frac by animateFloatAsState(
        targetValue = ratio.coerceIn(0f, 1f),
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "progress"
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Box(
            Modifier
                .fillMaxWidth(frac)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(color)
        )
    }
}

/** Anel de progresso circular (cronômetro de descanso, metas) */
@Composable
fun RingProgress(
    ratio: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    strokeWidth: Float = 9f,
    content: @Composable () -> Unit = {}
) {
    val frac by animateFloatAsState(
        targetValue = ratio.coerceIn(0f, 1f),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "ring"
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth * density, cap = StrokeCap.Round)
            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
            if (frac > 0.001f) {
                drawArc(color = color, startAngle = -90f, sweepAngle = 360f * frac, useCenter = false, style = stroke)
            }
        }
        content()
    }
}

/** Check com animação de "pop" — usado ao concluir sérias/exercícios */
@Composable
fun PopCheck(modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary, size: androidx.compose.ui.unit.Dp = 26.dp) {
    val scale = remember { Animatable(0.45f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
    Icon(
        Icons.Filled.CheckCircle,
        "Concluída",
        modifier = modifier
            .size(size)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        tint = tint
    )
}

/** Entrada suave de seções (fade + slide discreto) ao aparecer na tela */
@Composable
fun EnterSection(index: Int = 0, content: @Composable ColumnScope.() -> Unit) {
    val delayMs = (index * 55).coerceAtMost(400)
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    AnimatedVisibility(
        visible = started,
        enter = fadeIn(tween(300, delayMillis = delayMs)) +
            slideInVertically(
                animationSpec = tween(340, delayMillis = delayMs, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 5 }
            ),
        content = { Column(Modifier.fillMaxWidth(), content = content) }
    )
}

/** Estado vazio com ícone em círculo suave */
@Composable
fun EmptyState(text: String, icon: ImageVector = Icons.Filled.FitnessCenter, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Botão principal com leve escala ao tocar */
@Composable
fun PrimaryBigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    container: Color = MaterialTheme.colorScheme.primary,
    content: Color = MaterialTheme.colorScheme.onPrimary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(130), label = "press")
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content)
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

/** Botão secundário (contornado) com leve escala ao tocar */
@Composable
fun TonalButton2(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(130), label = "press")
    OutlinedButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .height(44.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

// ---------- Campos ----------

@Composable
fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Confirmar"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = { onDismiss(); onConfirm() }) { Text(confirmText, color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun RadioPickerDialog(
    title: String,
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                options.forEach { op ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .clickable { onSelect(op) }.padding(vertical = 10.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected == op, onClick = { onSelect(op) })
                        Text(op, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

// ---------- Gráficos ----------

/** Gráfico de barras com crescimento suave ao carregar */
@Composable
fun BarChart(
    data: List<Pair<String, Double>>,
    color: Color = MaterialTheme.colorScheme.primary,
    valueFmt: (Double) -> String = { com.gymtrack.app.data.Disp.fmtKg(it) },
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val max = (data.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(data) { progress.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(130.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            data.forEach { (label, v) ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (v > 0) Text(valueFmt(v), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    val target = (6f + 88f * (v / max).toFloat())
                    Box(
                        Modifier
                            .fillMaxWidth(0.66f)
                            .height((target * progress.value).dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (v > 0) color else MaterialTheme.colorScheme.surfaceContainerHighest)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

/** Gráfico de linha com desenho progressivo e área suave */
@Composable
fun LineChart(
    data: List<Pair<String, Double>>,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) {
        EmptyState("Poucos dados para o gráfico", Icons.Filled.TrendingUp)
        return
    }
    val max = (data.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)
    val min = (data.minOfOrNull { it.second } ?: 0.0).coerceAtMost(0.0)
    val range = (max - min).coerceAtLeast(1.0)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(data) { progress.animateTo(1f, tween(750, easing = FastOutSlowInEasing)) }
    Column(modifier.fillMaxWidth()) {
        androidx.compose.foundation.Canvas(
            Modifier.fillMaxWidth().height(110.dp)
        ) {
            val w = size.width
            val h = size.height
            val stepX = if (data.size > 1) w / (data.size - 1) else w
            val pts = data.mapIndexed { i, (_, v) ->
                androidx.compose.ui.geometry.Offset(i * stepX, h - ((v - min) / range * (h - 16)).toFloat() - 8f)
            }
            val path = Path()
            pts.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }
            val area = Path()
            area.moveTo(0f, h)
            pts.forEach { p -> area.lineTo(p.x, p.y) }
            area.lineTo(w, h)
            area.close()
            val clipW = w * progress.value
            clipRect(left = 0f, top = 0f, right = clipW, bottom = h) {
                drawPath(
                    area,
                    Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0f))
                    )
                )
                drawPath(path, color, style = Stroke(width = 5f, cap = StrokeCap.Round))
                pts.forEach { p -> drawCircle(color, radius = 7f, center = p) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            data.take(1).forEach { (l, _) -> Text(l, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            data.takeLast(1).forEach { (l, v) ->
                Text("${l}: ${com.gymtrack.app.data.Disp.fmtKg(v)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ---------- Seleção de dias da semana ----------

@Composable
fun WeekdayChips(
    selected: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        (1..7).forEach { iso ->
            val sel = selected.contains(iso)
            FilterChip(
                selected = sel,
                onClick = { onToggle(iso) },
                label = { Text(DateUtils.dowShort(iso)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TimeField(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { v ->
            val clean = v.filter { it.isDigit() || it == ':' }.take(5)
            onChange(clean)
        },
        label = { Text("Horário (HH:mm)") },
        placeholder = { Text("07:30") },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

// ---------- Diálogo de configuração de exercício do treino ----------

data class ExConfig(
    val sets: Int = 3,
    val repsMin: Int = 8,
    val repsMax: Int = 12,
    val weight: Double = 0.0,
    val restSeconds: Int = 90,
    val rir: String = "",
    val rpe: String = "",
    val tempo: String = "",
    val method: String = "",
    val notes: String = ""
)

@Composable
fun ExConfigDialog(
    initial: ExConfig,
    unit: String,
    title: String,
    onConfirm: (ExConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var sets by remember { mutableStateOf(initial.sets.toString()) }
    var repsMin by remember { mutableStateOf(initial.repsMin.toString()) }
    var repsMax by remember { mutableStateOf(initial.repsMax.toString()) }
    var weightTxt by remember {
        mutableStateOf(if (initial.weight > 0) com.gymtrack.app.data.Disp.fmtKg(com.gymtrack.app.data.Disp.fromKg(initial.weight, unit)) else "")
    }
    var rest by remember { mutableStateOf(initial.restSeconds.toString()) }
    var rir by remember { mutableStateOf(initial.rir) }
    var rpe by remember { mutableStateOf(initial.rpe) }
    var tempo by remember { mutableStateOf(initial.tempo) }
    var method by remember { mutableStateOf(initial.method) }
    var notes by remember { mutableStateOf(initial.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(
                Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledTextField(sets, { sets = it.filter { c -> c.isDigit() }.take(3) }, "Séries", Modifier.weight(1f), KeyboardType.Number)
                    LabeledTextField(repsMin, { repsMin = it.filter { c -> c.isDigit() }.take(3) }, "Reps mín.", Modifier.weight(1f), KeyboardType.Number)
                    LabeledTextField(repsMax, { repsMax = it.filter { c -> c.isDigit() }.take(3) }, "Reps máx.", Modifier.weight(1f), KeyboardType.Number)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledTextField(weightTxt, { weightTxt = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' }.take(7) }, "Carga ($unit)", Modifier.weight(1f), KeyboardType.Decimal)
                    LabeledTextField(rest, { rest = it.filter { c -> c.isDigit() }.take(4) }, "Descanso (s)", Modifier.weight(1f), KeyboardType.Number)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledTextField(rir, { rir = it.take(10) }, "RIR (ex.: 1–2)", Modifier.weight(1f))
                    LabeledTextField(rpe, { rpe = it.take(10) }, "RPE (ex.: 8)", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledTextField(tempo, { tempo = it.take(12) }, "Tempo (ex.: 2-0-1)", Modifier.weight(1f))
                }
                Row(
                    Modifier.fillMaxWidth().androidH().background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(10.dp)).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("", "Drop-set", "Bi-set", "Rest-pause", "Pirâmide", "Até a falha").forEach { m ->
                        FilterChip(selected = method == m, onClick = { method = m }, label = { Text(if (m == "") "Normal" else m, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                LabeledTextField(notes, { notes = it.take(300) }, "Observação", minLines = 2, singleLine = false)
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    ExConfig(
                        sets = sets.toIntOrNull()?.coerceIn(1, 30) ?: 3,
                        repsMin = repsMin.toIntOrNull()?.coerceIn(1, 200) ?: 8,
                        repsMax = repsMax.toIntOrNull()?.coerceIn(1, 200) ?: 12,
                        weight = if (weightTxt.isBlank()) 0.0 else com.gymtrack.app.data.Disp.toKg(weightTxt.toDoubleOrNull() ?: 0.0, unit),
                        restSeconds = rest.toIntOrNull()?.coerceIn(0, 900) ?: 90,
                        rir = rir, rpe = rpe, tempo = tempo, method = method, notes = notes
                    )
                )
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
