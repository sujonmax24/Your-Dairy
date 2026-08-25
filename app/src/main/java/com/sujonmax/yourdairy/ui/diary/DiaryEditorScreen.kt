package com.sujonmax.yourdairy.ui.diary

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.sujonmax.yourdairy.data.local.entity.FolderEntity
import com.sujonmax.yourdairy.data.local.entity.NoteEntity
import com.sujonmax.yourdairy.ui.media.AudioRecorderController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Delete
import java.io.File
import kotlinx.coroutines.delay

private val memoryMoods = listOf(
    "😊 Happy", "😍 Loved", "😢 Sad", "😡 Angry",
    "😰 Anxious", "😴 Tired", "🤩 Excited", "😐 Normal"
)

private val memoryBackgrounds = listOf(
    "default" to "Default",
    "sky" to "Sky",
    "mint" to "Mint",
    "peach" to "Peach",
    "rose" to "Rose",
    "lavender" to "Lavender",
    "sunset" to "Sunset",
    "night" to "Night"
)

private fun memoryBackgroundColor(key: String): Color = when (key) {
    "sky" -> Color(0xFFEAF5FF)
    "mint" -> Color(0xFFEAF8F0)
    "peach" -> Color(0xFFFFF0E7)
    "rose" -> Color(0xFFFFEAF0)
    "lavender" -> Color(0xFFF0ECFF)
    "sunset" -> Color(0xFFFFE8D6)
    "night" -> Color(0xFF172033)
    else -> Color.Transparent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEditorScreen(
    note: NoteEntity?,
    folders: List<FolderEntity>,
    onBack: () -> Unit,
    onSave: (NoteEntity) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var title by rememberSaveable(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var body by remember(note?.id) { mutableStateOf(TextFieldValue(note?.content.orEmpty())) }
    var tags by rememberSaveable(note?.id) { mutableStateOf(note?.tags.orEmpty()) }
    var folderId by rememberSaveable(note?.id) { mutableStateOf(note?.folderId) }
    var mood by rememberSaveable(note?.id) { mutableStateOf(note?.mood.orEmpty()) }
    var location by rememberSaveable(note?.id) { mutableStateOf(note?.location.orEmpty()) }
    var imageUris by rememberSaveable(note?.id) { mutableStateOf(note?.imageUris.orEmpty()) }
    var audioUris by rememberSaveable(note?.id) { mutableStateOf(note?.audioUris.orEmpty()) }
    var backgroundKey by rememberSaveable(note?.id) { mutableStateOf(note?.backgroundKey ?: "default") }
    var showTableDialog by rememberSaveable { mutableStateOf(false) }
    var showFolderDialog by rememberSaveable { mutableStateOf(false) }
    var showMoodDialog by rememberSaveable { mutableStateOf(false) }
    var showLocationDialog by rememberSaveable { mutableStateOf(false) }
    var showBackgroundDialog by rememberSaveable { mutableStateOf(false) }
    var showRecorderDialog by rememberSaveable { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }
    var playingAudio by remember { mutableStateOf<String?>(null) }

    val recorderController = remember { AudioRecorderController(context.applicationContext) }
    val mediaPlayer = remember { MediaPlayer() }
    val audioPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) showRecorderDialog = true
    }

    DisposableEffect(Unit) {
        onDispose {
            recorderController.cancel()
            runCatching { mediaPlayer.stop() }
            mediaPlayer.release()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) imageUris = listOf(imageUris, uri.toString()).filter { it.isNotBlank() }.joinToString("|")
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) audioUris = listOf(audioUris, uri.toString()).filter { it.isNotBlank() }.joinToString("|")
    }

    fun insertAtSelection(text: String) {
        val start = body.selection.start.coerceIn(0, body.text.length)
        val end = body.selection.end.coerceIn(start, body.text.length)
        val updated = body.text.replaceRange(start, end, text)
        body = TextFieldValue(updated, TextRange(start + text.length))
    }

    fun wrapSelection(prefix: String, suffix: String = prefix) {
        val start = body.selection.start.coerceIn(0, body.text.length)
        val end = body.selection.end.coerceIn(start, body.text.length)
        val selected = body.text.substring(start, end)
        val replacement = prefix + selected + suffix
        val updated = body.text.replaceRange(start, end, replacement)
        body = TextFieldValue(updated, TextRange(start + prefix.length, start + prefix.length + selected.length))
    }

    fun startRecording() {
        val dir = File(context.filesDir, "voice_memories")
        val file = File(dir, "voice_${System.currentTimeMillis()}.m4a")
        if (recorderController.start(file)) isRecording = true
    }

    fun stopRecording() {
        val file = recorderController.stop()
        isRecording = false
        if (file != null) {
            audioUris = listOf(audioUris, file.absolutePath).filter { it.isNotBlank() }.joinToString("|")
            showRecorderDialog = false
        }
    }

    fun deleteAudio(audio: String) {
        if (!audio.startsWith("content://")) File(audio).delete()
        audioUris = audioUris.split('|').filter { it.isNotBlank() && it != audio }.joinToString("|")
        if (playingAudio == audio) {
            runCatching { mediaPlayer.stop() }
            playingAudio = null
        }
    }

    fun togglePlayback(audio: String) {
        if (playingAudio == audio) {
            runCatching { mediaPlayer.stop() }
            playingAudio = null
            return
        }
        runCatching {
            mediaPlayer.reset()
            if (audio.startsWith("content://")) {
                context.contentResolver.openFileDescriptor(android.net.Uri.parse(audio), "r")?.use { mediaPlayer.setDataSource(it.fileDescriptor) }
            } else mediaPlayer.setDataSource(audio)
            mediaPlayer.setOnCompletionListener { playingAudio = null }
            mediaPlayer.prepare()
            mediaPlayer.start()
            playingAudio = audio
        }.onFailure { playingAudio = null }
    }

    val selectedFolder = folders.firstOrNull { it.id == folderId }
    val saveEnabled = title.isNotBlank() || body.text.isNotBlank()
    val pageBackground = memoryBackgroundColor(backgroundKey)
    val selectedBackgroundName = memoryBackgrounds.firstOrNull { it.first == backgroundKey }?.second ?: "Default"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (note == null) "New Memory" else "Edit Memory") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    IconButton(
                        onClick = {
                            onSave((note ?: NoteEntity()).copy(
                                title = title.trim(), content = body.text, tags = tags.trim(), folderId = folderId,
                                mood = mood.ifBlank { null }, location = location.trim().ifBlank { null },
                                imageUris = imageUris, audioUris = audioUris, backgroundKey = backgroundKey
                            ))
                        }, enabled = saveEnabled
                    ) { Icon(Icons.Default.Check, "Save memory") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().background(pageBackground).padding(padding).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Memory title") }, shape = RoundedCornerShape(14.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How did you feel?", style = MaterialTheme.typography.labelLarge)
                    AssistChip(onClick = { showMoodDialog = true }, label = { Text(mood.ifBlank { "Choose mood" }) })
                    OutlinedButton(onClick = { showBackgroundDialog = true }) { Text("🎨 Background: $selectedBackgroundName") }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Image, null); Spacer(Modifier.padding(2.dp)); Text("Photo") }
                OutlinedButton(onClick = { audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Mic, null); Spacer(Modifier.padding(2.dp)); Text("Record") }
                OutlinedButton(onClick = { audioPicker.launch(arrayOf("audio/*")) }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.AttachFile, null); Spacer(Modifier.padding(2.dp)); Text("Audio") }
            }
            val audioList = audioUris.split('|').filter { it.isNotBlank() }
            if (imageUris.isNotBlank() || audioList.isNotEmpty()) {
                Text("Attachments: ${(if (imageUris.isBlank()) 0 else imageUris.split('|').size)} photo(s), ${audioList.size} audio file(s)", style = MaterialTheme.typography.labelMedium)
                audioList.forEachIndexed { index, audio ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Voice ${index + 1}", modifier = Modifier.weight(1f), maxLines = 1)
                        IconButton(onClick = { togglePlayback(audio) }) { Icon(if (playingAudio == audio) Icons.Default.Stop else Icons.Default.PlayArrow, "Play voice") }
                        IconButton(onClick = { deleteAudio(audio) }) { Icon(Icons.Default.Delete, "Delete voice") }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showLocationDialog = true }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Place, null); Spacer(Modifier.padding(2.dp)); Text(location.ifBlank { "Add location" }, maxLines = 1) }
                OutlinedButton(onClick = { showFolderDialog = true }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Folder, null); Spacer(Modifier.padding(2.dp)); Text(selectedFolder?.name ?: "No folder", maxLines = 1) }
            }
            OutlinedTextField(value = tags, onValueChange = { tags = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Tags (comma separated)") })
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = { wrapSelection("**") }) { Text("Bold") }
                TextButton(onClick = { wrapSelection("_") }) { Text("Italic") }
                TextButton(onClick = { wrapSelection("__") }) { Text("Underline") }
                TextButton(onClick = { insertAtSelection("• ") }) { Text("Bullet") }
                TextButton(onClick = { insertAtSelection("1. ") }) { Text("Number") }
                TextButton(onClick = { insertAtSelection("# ") }) { Text("Heading") }
                TextButton(onClick = { insertAtSelection("> ") }) { Text("Quote") }
                TextButton(onClick = { showTableDialog = true }) { Icon(Icons.Default.AttachFile, null); Text("Table") }
            }
            HorizontalDivider()
            BasicTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier.fillMaxWidth().weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = if (backgroundKey == "night") Color.White else MaterialTheme.colorScheme.onBackground),
                decorationBox = { innerTextField ->
                    if (body.text.isEmpty()) Text("Write your memory...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    innerTextField()
                }
            )
        }
    }

    if (showBackgroundDialog) {
        AlertDialog(
            onDismissRequest = { showBackgroundDialog = false },
            title = { Text("Choose memory background") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    memoryBackgrounds.forEach { (key, label) ->
                        val selected = key == backgroundKey
                        Button(
                            onClick = { backgroundKey = key; showBackgroundDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (selected) "✓ $label" else label) }
                    }
                    Text("This background is saved only for this memory.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { showBackgroundDialog = false }) { Text("Close") } }
        )
    }

    if (showRecorderDialog) {
        AlertDialog(
            onDismissRequest = { if (!isRecording) showRecorderDialog = false },
            title = { Text(if (isRecording) "Recording voice memory" else "Voice memory") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (isRecording) "Recording: ${recordingSeconds / 60}:${(recordingSeconds % 60).toString().padStart(2, '0')}" else "Record a private voice memory.")
                if (isRecording) Text("Tap Stop when you are finished.")
            } },
            confirmButton = { Button(onClick = { if (isRecording) stopRecording() else startRecording() }) { Text(if (isRecording) "Stop & Save" else "Start Recording") } },
            dismissButton = { if (!isRecording) TextButton(onClick = { showRecorderDialog = false }) { Text("Cancel") } else TextButton(onClick = { recorderController.cancel(); isRecording = false; showRecorderDialog = false }) { Text("Discard") } }
        )
    }

    if (showMoodDialog) AlertDialog(onDismissRequest = { showMoodDialog = false }, title = { Text("Choose your mood") }, text = { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { memoryMoods.forEach { option -> TextButton(onClick = { mood = option; showMoodDialog = false }, modifier = Modifier.fillMaxWidth()) { Text(option) } } } }, confirmButton = { TextButton(onClick = { showMoodDialog = false }) { Text("Close") } })
    if (showLocationDialog) AlertDialog(onDismissRequest = { showLocationDialog = false }, title = { Text("Memory location") }, text = { OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Place or location") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }, confirmButton = { Button(onClick = { showLocationDialog = false }) { Text("Save") } }, dismissButton = { TextButton(onClick = { location = ""; showLocationDialog = false }) { Text("Clear") } })
    if (showFolderDialog) AlertDialog(onDismissRequest = { showFolderDialog = false }, title = { Text("Choose folder") }, text = { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { TextButton(onClick = { folderId = null; showFolderDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("No folder") }; folders.forEach { folder -> TextButton(onClick = { folderId = folder.id; showFolderDialog = false }, modifier = Modifier.fillMaxWidth()) { Text(folder.name) } }; if (folders.isEmpty()) Text("Create a folder from My Diary first.") } }, confirmButton = { TextButton(onClick = { showFolderDialog = false }) { Text("Close") } })
    if (showTableDialog) TableEditorDialog(onDismiss = { showTableDialog = false }, onSave = { table -> insertAtSelection(table); showTableDialog = false })
}
