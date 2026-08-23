package com.sujonmax.yourdairy.ui.diary

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.sujonmax.yourdairy.data.local.entity.NoteEntity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEditorScreen(
    note: NoteEntity?,
    onBack: () -> Unit,
    onSave: (NoteEntity) -> Unit
) {
    var title by rememberSaveable(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var body by remember(note?.id) { mutableStateOf(TextFieldValue(note?.content.orEmpty())) }
    var showTableDialog by rememberSaveable { mutableStateOf(false) }

    fun insertAtSelection(text: String) {
        val start = body.selection.start.coerceIn(0, body.text.length)
        val end = body.selection.end.coerceIn(start, body.text.length)
        val updated = body.text.replaceRange(start, end, text)
        val cursor = start + text.length
        body = TextFieldValue(updated, TextRange(cursor))
    }

    fun wrapSelection(prefix: String, suffix: String = prefix) {
        val start = body.selection.start.coerceIn(0, body.text.length)
        val end = body.selection.end.coerceIn(start, body.text.length)
        val selected = body.text.substring(start, end)
        val replacement = prefix + selected + suffix
        val updated = body.text.replaceRange(start, end, replacement)
        body = TextFieldValue(updated, TextRange(start + prefix.length, start + prefix.length + selected.length))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (note == null) "New Diary" else "Edit Diary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onSave(
                                (note ?: NoteEntity()).copy(
                                    title = title.trim(),
                                    content = body.text
                                )
                            )
                            onBack()
                        },
                        enabled = title.isNotBlank() || body.text.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save diary")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Title") }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TextButton(onClick = { wrapSelection("**") }) { Text("Bold") }
                TextButton(onClick = { wrapSelection("_") }) { Text("Italic") }
                TextButton(onClick = { wrapSelection("__") }) { Text("Underline") }
                TextButton(onClick = { insertAtSelection("• ") }) { Text("Bullet") }
                TextButton(onClick = { insertAtSelection("1. ") }) { Text("Number") }
                TextButton(onClick = { insertAtSelection("# ") }) { Text("Heading") }
                TextButton(onClick = { insertAtSelection("> ") }) { Text("Quote") }
                TextButton(onClick = { showTableDialog = true }) { Text("Table") }
            }

            HorizontalDivider()

            BasicTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                decorationBox = { innerTextField ->
                    if (body.text.isEmpty()) {
                        Text(
                            "Write your memory... Select text and use the toolbar above.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            )
        }
    }

    if (showTableDialog) {
        TableInsertDialog(
            onDismiss = { showTableDialog = false },
            onInsert = { rows, columns ->
                insertAtSelection(createMarkdownTable(rows, columns))
                showTableDialog = false
            }
        )
    }
}

@Composable
private fun TableInsertDialog(
    onDismiss: () -> Unit,
    onInsert: (Int, Int) -> Unit
) {
    var rows by rememberSaveable { mutableIntStateOf(2) }
    var columns by rememberSaveable { mutableIntStateOf(2) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert table") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Choose rows and columns")
                Stepper("Rows", rows, 1, 20) { rows = it }
                Stepper("Columns", columns, 1, 10) { columns = it }
            }
        },
        confirmButton = {
            Button(onClick = { onInsert(rows, columns) }) { Text("Insert") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun Stepper(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$label: $value")
        Row {
            TextButton(onClick = { if (value > min) onChange(value - 1) }) { Text("−") }
            TextButton(onClick = { if (value < max) onChange(value + 1) }) { Text("+") }
        }
    }
}

private fun createMarkdownTable(rows: Int, columns: Int): String {
    val header = (1..columns).joinToString(" | ", prefix = "| ", postfix = " |") { "Column $it" }
    val separator = (1..columns).joinToString(" | ", prefix = "| ", postfix = " |") { "---" }
    val data = (1 until rows).joinToString("\n") { row ->
        (1..columns).joinToString(" | ", prefix = "| ", postfix = " |") { "Row $row" }
    }
    return listOf(header, separator, data).filter { it.isNotBlank() }.joinToString("\n") + "\n\n"
}
