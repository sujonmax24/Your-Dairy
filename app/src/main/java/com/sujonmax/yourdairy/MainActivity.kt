package com.sujonmax.yourdairy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sujonmax.yourdairy.data.local.entity.NoteEntity
import com.sujonmax.yourdairy.ui.diary.DiaryEditorScreen
import com.sujonmax.yourdairy.ui.diary.DiaryViewModel
import com.sujonmax.yourdairy.ui.diary.DiaryViewModelFactory
import com.sujonmax.yourdairy.ui.theme.YourDairyTheme

class MainActivity : ComponentActivity() {
    private val diaryViewModel: DiaryViewModel by viewModels {
        DiaryViewModelFactory((application as DiaryApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourDairyTheme {
                DreamDiaryApp(diaryViewModel)
            }
        }
    }
}

@Composable
private fun DreamDiaryApp(viewModel: DiaryViewModel) {
    var editingNote by rememberSaveable { mutableStateOf<NoteEntity?>(null) }
    var isEditorOpen by rememberSaveable { mutableStateOf(false) }

    if (isEditorOpen) {
        DiaryEditorScreen(
            note = editingNote,
            onBack = { isEditorOpen = false },
            onSave = { note ->
                val existing = editingNote
                if (existing == null) {
                    viewModel.saveNote(note.title, note.content)
                } else {
                    viewModel.updateNote(existing, note.title, note.content)
                }
            }
        )
    } else {
        DreamDiaryHome(
            viewModel = viewModel,
            onNewNote = {
                editingNote = null
                isEditorOpen = true
            },
            onEditNote = { note ->
                editingNote = note
                isEditorOpen = true
            }
        )
    }
}

@Composable
private fun DreamDiaryHome(
    viewModel: DiaryViewModel,
    onNewNote: () -> Unit,
    onEditNote: (NoteEntity) -> Unit
) {
    val notes by viewModel.searchResults.collectAsStateWithLifecycle(initialValue = emptyList())
    val query by viewModel.query.collectAsStateWithLifecycle()
    var searchMode by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dream Diry", fontWeight = FontWeight.Bold)
                        Text("create by sujonmax", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = { searchMode = !searchMode }) {
                        Icon(Icons.Default.Search, contentDescription = "Search diary")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewNote) {
                Icon(Icons.Default.Add, contentDescription = "New diary entry")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (searchMode) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setQuery,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
            }

            Text("Recent memories", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            if (notes.isEmpty()) {
                Text(
                    "No diary entries yet. Tap + to write your first memory.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(note = note, onClick = { onEditNote(note) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(note: NoteEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = note.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = note.content.ifBlank { "No content" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4
            )
        }
    }
}
