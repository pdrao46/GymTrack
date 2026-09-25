package com.gymtrack.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    if (onClick != null) {
        Surface(modifier = modifier.clip(shape).clickable { onClick() }, shape = shape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
            Column(Modifier.padding(16.dp), content = content)
        }
    } else {
        Surface(modifier = modifier, shape = shape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text, modifier = modifier.padding(start = 4.dp),
        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
    )
}

@Composable
fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FilledProgressBar(ratio: Float, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Box(
        modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            Modifier
                .fillMaxWidth(ratio.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(color)
        )
    }
}

@Composable
fun EmptyState(text: String, icon: ImageVector = Icons.Filled.FitnessCenter, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

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
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content)
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun TonalButton2(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text)
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

@Composable
fun BarChart(
    data: List<Pair<String, Double>>,
    color: Color = MaterialTheme.colorScheme.primary,
    valueFmt: (Double) -> String = { com.gymtrack.app.data.Disp.fmtKg(it) },
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val max = (data.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(130.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            data.forEach { (label, v) ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (v > 0) Text(valueFmt(v), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Box(
                        Modifier
                            .fillMaxWidth(0.66f)
                            .height((6 + 88 * (v / max)).dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (v > 0) color else MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

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
            drawPath(path, color, style = Stroke(width = 5f, cap = StrokeCap.Round))
            pts.forEach { p ->
                drawCircle(color, radius = 7f, center = p)
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
                    Modifier.fillMaxWidth().androidH().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(8.dp),
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
