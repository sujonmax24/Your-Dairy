package com.sujonmax.yourdairy.ui.txt

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun TxtEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var fileName by rememberSaveable { mutableStateOf("new-note.txt") }
    var text by rememberSaveable { mutableStateOf("") }
    var currentUri by remember { mutableStateOf<Uri?>(null) }

    fun show(message: String) {
        scope.launch { snackbar.showSnackbar(message) }
    }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val loaded = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: ""
            text = loaded
            currentUri = uri
            fileName = uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "opened-note.txt" } ?: "opened-note.txt"
            show("TXT file opened")
        } catch (_: Exception) {
            show("Could not open this TXT file")
        }
    }

    val saveAsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(text.toByteArray(Charsets.UTF_8))
            } ?: error("Unable to open output stream")
            currentUri = uri
            fileName = uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { fileName } ?: fileName
            show("TXT saved successfully")
        } catch (_: Exception) {
            show("Could not save the TXT file")
        }
    }

    fun saveCurrent() {
        val uri = currentUri
        if (uri == null) {
            saveAsLauncher.launch(if (fileName.endsWith(".txt", true)) fileName else "$fileName.txt")
            return
        }
        try {
            context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
                output.write(text.toByteArray(Charsets.UTF_8))
            } ?: error("Unable to open output stream")
            show("TXT saved successfully")
        } catch (_: Exception) {
            show("Could not save the TXT file. Use Save As instead.")
        }
    }

    fun shareText() {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(send, "Share TXT"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TXT Editor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { openLauncher.launch(arrayOf("text/plain", "text/*")) }) {
                        Icon(Icons.Default.FileOpen, contentDescription = "Open TXT")
                    }
                    IconButton(onClick = ::saveCurrent) {
                        Icon(Icons.Default.Save, contentDescription = "Save TXT")
                    }
                    IconButton(onClick = ::shareText) {
                        Icon(Icons.Default.Share, contentDescription = "Share TXT")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("UTF-8 TXT editor", style = MaterialTheme.typography.titleMedium)
            Text("Bengali and other Unicode text are saved as UTF-8.", style = MaterialTheme.typography.bodySmall)

            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = { Text("File name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Write your text") },
                modifier = Modifier.fillMaxWidth().height(420.dp)
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    text = ""
                    fileName = "new-note.txt"
                    currentUri = null
                }, modifier = Modifier.weight(1f)) {
                    Text("New")
                }
                OutlinedButton(onClick = {
                    saveAsLauncher.launch(if (fileName.endsWith(".txt", true)) fileName else "$fileName.txt")
                }, modifier = Modifier.weight(1f)) {
                    Text("Save As")
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Tip: Use Save As when creating a new TXT file or when the original file cannot be overwritten.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
