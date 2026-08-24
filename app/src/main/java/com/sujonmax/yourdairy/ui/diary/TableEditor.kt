package com.sujonmax.yourdairy.ui.diary

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private const val MAX_ROWS = 20
private const val MAX_COLUMNS = 10

@Composable
fun TableEditorDialog(
    initialTable: String? = null,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val initial = remember(initialTable) { parseMarkdownTable(initialTable) }
    val rows = remember(initialTable) {
        mutableStateListOf<MutableList<String>>().apply {
            addAll(initial.map { it.toMutableList() })
        }
    }
    var error by remember { mutableStateOf<String?>(null) }

    fun normalize() {
        val columnCount = rows.maxOfOrNull { it.size }?.coerceIn(1, MAX_COLUMNS) ?: 1
        rows.forEachIndexed { index, row ->
            while (row.size < columnCount) row.add("")
            while (row.size > columnCount) row.removeAt(row.lastIndex)
            rows[index] = row
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTable == null) "Create table" else "Edit table") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        if (rows.size < MAX_ROWS) rows.add(MutableList(rows.firstOrNull()?.size ?: 1) { "" })
                    }) { Text("+ Row") }
                    Button(onClick = {
                        val columns = rows.firstOrNull()?.size ?: 1
                        if (columns < MAX_COLUMNS) rows.forEach { it.add("") }
                    }) { Text("+ Column") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { if (rows.size > 1) rows.removeAt(rows.lastIndex) }) { Text("Delete Row") }
                    TextButton(onClick = {
                        val columns = rows.firstOrNull()?.size ?: 1
                        if (columns > 1) rows.forEach { it.removeAt(it.lastIndex) }
                        normalize()
                    }) { Text("Delete Column") }
                }
                Text("Rows: ${rows.size} • Columns: ${rows.firstOrNull()?.size ?: 0}", style = MaterialTheme.typography.labelMedium)
                Column(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rows.forEachIndexed { rowIndex, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEachIndexed { columnIndex, value ->
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { row[columnIndex] = it },
                                    modifier = Modifier.padding(1.dp),
                                    label = { Text("${rowIndex + 1},${columnIndex + 1}") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                                )
                            }
                        }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                normalize()
                if (rows.isEmpty() || rows.firstOrNull()?.isEmpty() != false) {
                    error = "Table must contain at least one cell."
                } else {
                    onSave(toMarkdown(rows))
                }
            }) { Text("Save table") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun toMarkdown(rows: List<List<String>>): String {
    if (rows.isEmpty()) return ""
    val columns = rows.maxOfOrNull { it.size } ?: return ""
    val normalized = rows.map { row ->
        (0 until columns).map { index ->
            row.getOrNull(index).orEmpty().replace("|", "\\|").replace("\n", " ")
        }
    }
    val header = normalized.first().joinToString(" | ", prefix = "| ", postfix = " |")
    val separator = (1..columns).joinToString(" | ", prefix = "| ", postfix = " |") { "---" }
    val body = normalized.drop(1).joinToString("\n") { it.joinToString(" | ", prefix = "| ", postfix = " |") }
    return listOf(header, separator, body).filter { it.isNotBlank() }.joinToString("\n")
}

private fun parseMarkdownTable(markdown: String?): List<List<String>> {
    if (markdown.isNullOrBlank()) return List(2) { MutableList(2) { "" } }
    val lines = markdown.lines().filter { it.trim().startsWith("|") && it.trim().endsWith("|") }
    if (lines.isEmpty()) return List(2) { MutableList(2) { "" } }
    return lines.filterIndexed { index, _ -> index != 1 || !lines.getOrNull(1).orEmpty().contains("---") }
        .map { line ->
            line.trim().removePrefix("|").removeSuffix("|").split("|")
                .map { it.trim().replace("\\|", "|") }
                .toMutableList()
        }
        .ifEmpty { List(2) { MutableList(2) { "" } } }
}
