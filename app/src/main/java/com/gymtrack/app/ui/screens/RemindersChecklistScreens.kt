package com.gymtrack.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.rem.ReminderScheduler
import com.gymtrack.app.ui.*

// ================= LEMBRETES =================

@Composable
fun RemindersScreen(nav: NavHostController) {
    val tick = refreshTick()
    val context = LocalContext.current
    var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }
    var showNew by remember { mutableStateOf(false) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { reminders = Repo.reminders() }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
                Text("Lembretes 🔔", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(
                "Receba avisos como \"Seu treino começa em 30 minutos\" nos dias e horários escolhidos. Se as notificações do app estiverem desativadas no sistema, os lembretes não aparecem.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )
            if (reminders.isEmpty()) {
                AppCard(Modifier.fillMaxWidth()) {
                    EmptyState("Nenhum lembrete. Crie um para não perder o treino!", Icons.Filled.Notifications)
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 90.dp)) {
                items(reminders.size) { i ->
                    val r = reminders[i]
                    AppCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Alarm,
                                null,
                                tint = if (r.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(r.title.ifBlank { "Lembrete" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                val days = r.days.split(",").mapNotNull { it.trim().toIntOrNull() }.sorted()
                                val daysTxt = when {
                                    days.size == 7 -> "Todos os dias"
                                    days.isEmpty() -> "Sem dias"
                                    else -> days.joinToString(", ") { DateUtils.dowShort(it) }
                                }
                                Text(
                                    "$daysTxt · ${r.hour.toString().padStart(2, '0')}:${r.minute.toString().padStart(2, '0')}",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = r.enabled,
                                onCheckedChange = { on ->
                                    val updated = r.copy(enabled = on)
                                    Graph.launch {
                                        Repo.updateReminder(updated)
                                        ReminderScheduler.rescheduleOne(context, updated)
                                    }
                                }
                            )
                            IconButton(onClick = {
                                Graph.launch {
                                    Repo.deleteReminder(r.id)
                                    ReminderScheduler.rescheduleAll(context)
                                }
                            }) { Icon(Icons.Filled.Delete, "Excluir", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { showNew = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(6.dp))
            Text("Novo lembrete")
        }
    }

    if (showNew) {
        var title by remember { mutableStateOf("Treino 💪") }
        var hourTxt by remember { mutableStateOf("19") }
        var minTxt by remember { mutableStateOf("00") }
        var days by remember { mutableStateOf(setOf(1, 3, 5)) }

        AlertDialog(
            onDismissRequest = { showNew = false },
            title = { Text("Novo lembrete", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabeledTextField(title, { title = it.take(60) }, "Título (ex.: Treino, Medidas...)")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabeledTextField(hourTxt, { hourTxt = it.filter { c -> c.isDigit() }.take(2) }, "Hora (0–23)", Modifier.weight(1f), NumKeyboard())
                        LabeledTextField(minTxt, { minTxt = it.filter { c -> c.isDigit() }.take(2) }, "Minutos", Modifier.weight(1f), NumKeyboard())
                    }
                    Text("Dias da semana", style = MaterialTheme.typography.labelMedium)
                    WeekdayChips(days) { iso -> days = if (days.contains(iso)) days - iso else days + iso }
                    TextButton(onClick = { days = setOf(1, 2, 3, 4, 5, 6, 7) }) { Text("Selecionar todos os dias") }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val h = (hourTxt.toIntOrNull() ?: 7).coerceIn(0, 23)
                    val m = (minTxt.toIntOrNull() ?: 0).coerceIn(0, 59)
                    Graph.launch {
                        val r = Reminder(
                            title = title.ifBlank { "Lembrete" }, type = "personalizado",
                            hour = h, minute = m,
                            days = days.sorted().joinToString(","), enabled = true
                        )
                        val id = Repo.insertReminder(r)
                        ReminderScheduler.rescheduleOne(context, r.copy(id = id))
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { showNew = false }
                    }
                }) { Text("Criar") }
            },
            dismissButton = { TextButton(onClick = { showNew = false }) { Text("Cancelar") } }
        )
    }
}

private fun NumKeyboard() = androidx.compose.ui.text.input.KeyboardType.Number

// ================= CHECKLIST PRÉ/PÓS-TREINO =================

@Composable
fun ChecklistScreen(nav: NavHostController) {
    val tick = refreshTick()
    var tab by remember { mutableStateOf(0) }
    var pre by remember { mutableStateOf<List<ChecklistItem>>(emptyList()) }
    var post by remember { mutableStateOf<List<ChecklistItem>>(emptyList()) }
    var newText by remember { mutableStateOf("") }

    LaunchedEffect(tick, tab) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            pre = Repo.checklist("pre")
            post = Repo.checklist("post")
        }
    }

    val items = if (tab == 0) pre else post
    val category = if (tab == 0) "pre" else "post"

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Checklist", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Antes do treino") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Depois do treino") })
        }
        Spacer(Modifier.height(12.dp))

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            items.forEach { item ->
                Row(
                    Modifier.fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                        .clickable {
                            Graph.launch { Repo.setChecklistChecked(item.id, !item.checked) }
                        }.padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = item.checked, onCheckedChange = { on ->
                        Graph.launch { Repo.setChecklistChecked(item.id, on) }
                    })
                    Text(
                        item.label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        textDecoration = if (item.checked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                        color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { Graph.launch { Repo.deleteChecklistItem(item.id) } }) {
                        Icon(Icons.Filled.Close, "Remover", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (items.isEmpty()) {
                Text("Nenhum item. Adicione abaixo!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledTextField(newText, { newText = it.take(60) }, "Novo item (ex.: garrafa de água)", Modifier.weight(1f))
                Button(onClick = {
                    if (newText.isNotBlank()) {
                        Graph.launch {
                            Repo.addChecklistItem(category, newText.trim())
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { newText = "" }
                        }
                    }
                }) { Text("Add") }
            }
            Spacer(Modifier.height(10.dp))
            TonalButton2("RESETAR MARCAÇÕES", {
                Graph.launch { Repo.resetChecklist(category) }
            }, Modifier.fillMaxWidth(), Icons.Filled.RestartAlt)
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun Modifier.clip14(): Modifier = this.then(
    Modifier.clipPriv()
)

@Composable
private fun Modifier.clipPriv(): Modifier = androidx.compose.ui.draw.clip(
    androidx.compose.foundation.shape.RoundedCornerShape(14.dp), this
)
