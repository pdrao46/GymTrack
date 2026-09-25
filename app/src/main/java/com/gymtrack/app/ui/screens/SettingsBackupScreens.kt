package com.gymtrack.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.*
import com.gymtrack.app.ui.nav.Routes
import java.io.File

@Composable
fun SettingsScreen(nav: NavHostController) {
    val context = LocalContext.current
    val settings = LocalAppSettings.current

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Configurações", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))

        AppCard(Modifier.fillMaxWidth()) {
            Text("GERAL", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            var nameTxt by remember(settings.appName) { mutableStateOf(settings.appName) }
            LabeledTextField(nameTxt, { nameTxt = it.take(30) }, "Nome do aplicativo (aparece no início)")
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = {
                Graph.launch {
                    SettingsRepo.save(context) { it[SettingsRepo.KEY_NAME] = nameTxt.ifBlank { "GymTrack" } }
                }
            }) { Text("Salvar nome") }

            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            Text("Seu nome (saudação no início)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            var userNameTxt by remember(settings.userName) { mutableStateOf(settings.userName) }
            LabeledTextField(userNameTxt, { userNameTxt = it.take(30) }, "Ex.: Pedro")
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = {
                Graph.launch {
                    SettingsRepo.save(context) { it[SettingsRepo.KEY_USERNAME] = userNameTxt.trim() }
                }
            }) { Text("Salvar meu nome") }

            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            Text("Tema", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            listOf(0 to "Seguir o sistema", 1 to "Claro ☀️", 2 to "Escuro 🌙").forEach { (v, label) ->
                Row(Modifier.fillMaxWidth().clickable {
                    Graph.launch { SettingsRepo.save(context) { it[SettingsRepo.KEY_THEME] = v } }
                }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = settings.theme == v, onClick = {
                        Graph.launch { SettingsRepo.save(context) { it[SettingsRepo.KEY_THEME] = v } }
                    })
                    Text(label)
                }
            }
        }

        AppCard(Modifier.fillMaxWidth()) {
            Text("UNIDADES", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            Text("Unidade de peso", style = MaterialTheme.typography.bodyMedium)
            Row {
                listOf("kg" to "Quilogramas (kg)", "lb" to "Libras (lb)").forEach { (v, label) ->
                    Row(Modifier.weight(1f).clickable {
                        Graph.launch { SettingsRepo.save(context) { it[SettingsRepo.KEY_UNIT] = v } }
                    }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = settings.unit == v, onClick = {
                            Graph.launch { SettingsRepo.save(context) { it[SettingsRepo.KEY_UNIT] = v } }
                        })
                        Text(label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("Primeiro dia da semana", style = MaterialTheme.typography.bodyMedium)
            Row {
                listOf(false to "Segunda-feira", true to "Domingo").forEach { (v, label) ->
                    Row(Modifier.weight(1f).clickable {
                        Graph.launch { SettingsRepo.save(context) { it[SettingsRepo.KEY_FIRST_SUNDAY] = v } }
                    }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = settings.firstDaySunday == v, onClick = {
                            Graph.launch { SettingsRepo.save(context) { it[SettingsRepo.KEY_FIRST_SUNDAY] = v } }
                        })
                        Text(label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        AppCard(Modifier.fillMaxWidth()) {
            Text("APARÊNCIA DOS CARDS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().clickable {
                Graph.launch { SettingsRepo.save(context) { it[SettingsRepo.KEY_CARDS] = !settings.comfortableCards } }
            }, verticalAlignment = Alignment.CenterVertically) {
                Text("Cards confortáveis (mais espaçados)", modifier = Modifier.weight(1f))
                Switch(checked = settings.comfortableCards, onCheckedChange = { v ->
                    Graph.launch { SettingsRepo.save(context) { prefs -> prefs[SettingsRepo.KEY_CARDS] = v } }
                })
            }
        }

        AppCard(Modifier.fillMaxWidth()) {
            Text("NOTIFICAÇÕES", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            SettingSwitch("Notificações (lembretes e avisos)", settings.notificationsEnabled) { v ->
                Graph.launch { SettingsRepo.save(context) { it[SettingsRepo.KEY_NOTIF] = v } }
            }
            SettingSwitch("Som ao terminar o descanso", settings.soundEnabled) { v ->
                Graph.launch { SettingsRepo.save(context) { it[SettingsRepo.KEY_SOUND] = v } }
            }
            SettingSwitch("Vibração ao terminar o descanso", settings.vibrateEnabled) { v ->
                Graph.launch { SettingsRepo.save(context) { it[SettingsRepo.KEY_VIBRATE] = v } }
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = { nav.navigate(Routes.REMINDERS) }) {
                Icon(Icons.Filled.Alarm, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Configurar lembretes de treino")
            }
        }

        AppCard(Modifier.fillMaxWidth()) {
            Text("DADOS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { nav.navigate(Routes.BACKUP) }) {
                Icon(Icons.Filled.SaveAlt, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Backup: exportar e importar")
            }
            TextButton(onClick = { nav.navigate(Routes.PRIVACY) }) {
                Icon(Icons.Filled.Lock, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Privacidade e dados")
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "GymTrack v1.0.0 — 100% offline. Seus dados ficam apenas neste aparelho.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingSwitch(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = value, onCheckedChange = onChange)
    }
}

// ================= BACKUP =================

@Composable
fun BackupScreen(nav: NavHostController) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("") }
    var confirmImport by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (text != null) confirmImport = text
            } catch (e: Exception) {
                status = "Erro ao ler arquivo: ${e.message}"
            }
        }
    }

    fun shareFile(name: String, content: String, mime: String) {
        try {
            val dir = File(context.filesDir, "exports")
            if (!dir.exists()) dir.mkdirs()
            val f = File(dir, name)
            f.writeText(content)
            val uri = FileProvider.getUriForFile(context, context.packageName + ".files", f)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartilhar backup"))
            status = "Arquivo gerado: $name. Escolha onde salvar (Drive, arquivos, e-mail...)."
        } catch (e: Exception) {
            status = "Erro ao exportar: ${e.message}"
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Backup", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Exporte seus dados para não perderem em troca de celular. O arquivo JSON contém tudo e pode ser importado de volta. O CSV abre em Excel/Planilhas.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))

        AppCard(Modifier.fillMaxWidth()) {
            Text("EXPORTAR DADOS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            PrimaryBigButton("EXPORTAR JSON (backup completo)", {
                Graph.launch {
                    val json = Backup.exportJson()
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        shareFile("gymtrack_backup.json", json, "application/json")
                    }
                }
            }, icon = Icons.Filled.FileDownload)
            Spacer(Modifier.height(8.dp))
            TonalButton2("EXPORTAR CSV (planilha)", {
                Graph.launch {
                    val csv = Backup.exportCsv()
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        shareFile("gymtrack_treinos.csv", csv, "text/csv")
                    }
                }
            }, Modifier.fillMaxWidth(), Icons.Filled.TableChart)
        }

        Spacer(Modifier.height(12.dp))
        AppCard(Modifier.fillMaxWidth()) {
            Text("IMPORTAR DADOS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(6.dp))
            Text(
                "A importação de um arquivo JSON substitui todos os treinos, histórico e configurações atuais pelos dados do arquivo.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            TonalButton2("ESCOLHER ARQUIVO JSON", { importLauncher.launch("*/*") }, Modifier.fillMaxWidth(), Icons.Filled.FileUpload)
        }

        if (status.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            AppCard(Modifier.fillMaxWidth()) { Text(status, style = MaterialTheme.typography.bodyMedium) }
        }
        Spacer(Modifier.height(24.dp))
    }

    confirmImport?.let { text ->
        ConfirmDialog(
            "Importar backup?",
            "Todos os dados atuais de treinos, histórico e metas serão substituídos pelos dados do arquivo. Esta ação não pode ser desfeita.",
            confirmText = "Importar",
            onConfirm = {
                Graph.launch {
                    val ok = try {
                        Backup.importJson(text)
                        true
                    } catch (e: Exception) {
                        false
                    }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        status = if (ok) "Backup importado com sucesso! ✅" else "Arquivo inválido ou corrompido ❌"
                    }
                }
            },
            onDismiss = { confirmImport = null }
        )
    }
}

// ================= PRIVACIDADE =================

@Composable
fun PrivacyScreen(nav: NavHostController) {
    var confirmWipe by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
            Text("Privacidade e dados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))

        AppCard(Modifier.fillMaxWidth()) {
            Text("🔒 Seus dados são seus", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(
                "O GymTrack funciona 100% offline. Todos os dados — treinos, cargas, medidas, anotações e fotos — ficam armazenados apenas na memória deste aparelho.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        AppCard(Modifier.fillMaxWidth()) {
            Text("🚫 Sem cadastro, sem servidores", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(
                "O aplicativo não exige conta, não pede e-mail, não envia informações pessoais para servidores e não exibe anúncios. Internet não é necessária para nenhuma função.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        AppCard(Modifier.fillMaxWidth()) {
            Text("📷 Fotos privadas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(
                "As fotos de evolução são salvas em uma pasta interna do aplicativo, invisíveis para outros apps e para a galeria, a menos que você as compartilhe manualmente.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        AppCard(Modifier.fillMaxWidth()) {
            Text("🧹 Você tem o controle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(
                "Você pode exportar todos os dados (JSON/CSV) na área de Backup a qualquer momento, ou apagar tudo com o botão abaixo. Desinstalar o app também remove todos os dados.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(10.dp))
            TonalButton2("APAGAR TODOS OS DADOS", { confirmWipe = true }, Modifier.fillMaxWidth(), Icons.Filled.DeleteForever)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (confirmWipe) {
        ConfirmDialog(
            "Apagar todos os dados?",
            "Treinos, histórico, medidas, metas, anotações, lembretes e fotos serão apagados permanentemente deste aparelho. Esta ação não pode ser desfeita.",
            confirmText = "Apagar tudo",
            onConfirm = {
                Graph.launch {
                    Graph.db!!.wipeAll()
                    RefreshBus.bump()
                }
            },
            onDismiss = { confirmWipe = false }
        )
    }
}
