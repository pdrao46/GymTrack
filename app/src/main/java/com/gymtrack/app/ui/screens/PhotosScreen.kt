package com.gymtrack.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymtrack.app.data.*
import com.gymtrack.app.ui.*
import java.io.File
import java.util.UUID

@Composable
fun PhotosScreen(nav: NavHostController) {
    val tick = refreshTick()
    val context = LocalContext.current
    var photos by remember { mutableStateOf<List<ProgressPhoto>>(emptyList()) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var pendingDay by remember { mutableStateOf(DateUtils.today()) }
    var note by remember { mutableStateOf("") }
    var viewing by remember { mutableStateOf<ProgressPhoto?>(null) }
    var confirmDelete by remember { mutableStateOf<ProgressPhoto?>(null) }

    LaunchedEffect(tick) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { photos = Repo.photos() }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            pendingUri = uri
            note = ""
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Voltar") }
                Text("Fotos de evolução", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Surface(
                Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Privacidade: as fotos ficam salvas apenas neste aparelho, na memória interna do app. Nada é enviado para servidores.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (photos.isEmpty()) {
                AppCard(Modifier.fillMaxWidth()) {
                    EmptyState("Nenhuma foto ainda. Adicione a primeira para acompanhar sua evolução visual!", Icons.Filled.PhotoCamera)
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(3), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 90.dp)) {
                    items(photos.size) { i ->
                        val p = photos[i]
                        val f = File(p.path)
                        if (f.exists()) {
                            val bmp = remember(p.id) { decodeSampled(f.path, 300) }
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Foto ${DateUtils.fmtDate(p.dateEpochDay)}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.aspectRatio(0.8f).clip(RoundedCornerShape(14.dp)).clickable { viewing = p }
                                )
                            }
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.AddAPhoto, null)
            Spacer(Modifier.width(6.dp))
            Text("Adicionar")
        }
    }

    // diálogo ao escolher foto
    pendingUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingUri = null },
            title = { Text("Salvar foto", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabeledTextField(DateUtils.fmtDate(pendingDay), { v -> parseDateSimple(v)?.let { pendingDay = it } }, "Data (dd/mm/aaaa)")
                    LabeledTextField(note, { note = it.take(200) }, "Observação (opcional)", minLines = 2, singleLine = false)
                }
            },
            confirmButton = {
                Button(onClick = {
                    Graph.launch {
                        val dir = File(context.filesDir, "photos")
                        if (!dir.exists()) dir.mkdirs()
                        val dest = File(dir, "${UUID.randomUUID()}.jpg")
                        try {
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                dest.outputStream().use { output -> input.copyTo(output) }
                            }
                            Repo.insertPhoto(ProgressPhoto(dateEpochDay = pendingDay, path = dest.absolutePath, note = note))
                        } catch (_: Exception) {
                        }
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { pendingUri = null }
                    }
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { pendingUri = null }) { Text("Cancelar") } }
        )
    }

    // visualizar foto
    viewing?.let { p ->
        AlertDialog(
            onDismissRequest = { viewing = null },
            title = { Text(DateUtils.fmtLong(p.dateEpochDay), style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    val f = File(p.path)
                    if (f.exists()) {
                        val bmp = remember(p.id) { decodeSampled(f.path, 900) }
                        if (bmp != null) {
                            Image(bitmap = bmp.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    if (p.note.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(p.note, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { confirmDelete = p; viewing = null }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { viewing = null }) { Text("Fechar") } }
        )
    }

    confirmDelete?.let { p ->
        ConfirmDialog(
            "Excluir foto?",
            "A foto de ${DateUtils.fmtDate(p.dateEpochDay)} será apagada do aparelho.",
            confirmText = "Excluir",
            onConfirm = {
                Graph.launch {
                    try { File(p.path).delete() } catch (_: Exception) {}
                    Repo.deletePhoto(p.id)
                }
            },
            onDismiss = { confirmDelete = null }
        )
    }
}

private fun parseDateSimple(s: String): Long? {
    val p = s.split("/")
    if (p.size != 3) return null
    val d = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    val y = p[2].toIntOrNull() ?: return null
    return try { java.time.LocalDate.of(y, m, d).toEpochDay() } catch (e: Exception) { null }
}

private fun decodeSampled(path: String, reqSize: Int): android.graphics.Bitmap? {
    return try {
        val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(path, opts)
        var sample = 1
        while (opts.outHeight / (sample * 2) >= reqSize || opts.outWidth / (sample * 2) >= reqSize) sample *= 2
        android.graphics.BitmapFactory.decodeFile(path, android.graphics.BitmapFactory.Options().apply { inSampleSize = sample })
    } catch (e: Exception) {
        null
    }
}
