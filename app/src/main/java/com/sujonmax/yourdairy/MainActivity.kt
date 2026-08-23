package com.sujonmax.yourdairy

import android.os.Bundle
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.sujonmax.yourdairy.data.local.entity.NoteEntity
import com.sujonmax.yourdairy.security.BiometricHelper
import com.sujonmax.yourdairy.security.SecurityManager
import com.sujonmax.yourdairy.ui.diary.DiaryEditorScreen
import com.sujonmax.yourdairy.ui.diary.DiaryViewModel
import com.sujonmax.yourdairy.ui.diary.DiaryViewModelFactory
import com.sujonmax.yourdairy.ui.security.PinLockScreen
import com.sujonmax.yourdairy.ui.security.PinSetupScreen
import com.sujonmax.yourdairy.ui.security.RecoveryScreen
import com.sujonmax.yourdairy.ui.theme.YourDairyTheme

class MainActivity : FragmentActivity() {
    private val diaryViewModel: DiaryViewModel by viewModels {
        DiaryViewModelFactory((application as DiaryApplication).repository)
    }
    private lateinit var security: SecurityManager
    private var unlocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        security = SecurityManager(applicationContext)
        setContent {
            YourDairyTheme {
                SecurityGate()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (::security.isInitialized && security.isConfigured) unlocked = false
    }

    @Composable
    private fun SecurityGate() {
        var recoveryMode by rememberSaveable { mutableStateOf(false) }

        when {
            !security.isConfigured -> PinSetupScreen(security) { unlocked = true }
            recoveryMode -> RecoveryScreen(
                security = security,
                onRecovered = { recoveryMode = false; unlocked = true },
                onCancel = { recoveryMode = false }
            )
            !unlocked -> PinLockScreen(
                security = security,
                onUnlocked = { unlocked = true },
                onBiometric = if (security.biometricEnabled && BiometricHelper.isAvailable(this@MainActivity)) {
                    { BiometricHelper.authenticate(this@MainActivity, { unlocked = true }, {}) }
                } else null,
                onRecovery = { recoveryMode = true }
            )
            else -> DreamDiaryApp(diaryViewModel)
        }
    }

    @Composable
    private fun DreamDiaryApp(viewModel: DiaryViewModel) {
        var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
        var isEditorOpen by rememberSaveable { mutableStateOf(false) }

        if (isEditorOpen) {
            DiaryEditorScreen(
                note = editingNote,
                onBack = { isEditorOpen = false },
                onSave = { note ->
                    val existing = editingNote
                    if (existing == null) viewModel.saveNote(note.title, note.content)
                    else viewModel.updateNote(existing, note.title, note.content)
                    isEditorOpen = false
                }
            )
        } else {
            DreamDiaryHome(
                viewModel = viewModel,
                onNewNote = { editingNote = null; isEditorOpen = true },
                onEditNote = { note -> editingNote = note; isEditorOpen = true }
            )
        }
    }
}

@Composable
private fun DreamDiaryHome(viewModel: DiaryViewModel, onNewNote: () -> Unit, onEditNote: (NoteEntity) -> Unit) {
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
                actions = { IconButton(onClick = { searchMode = !searchMode }) { Icon(Icons.Default.Search, "Search diary") } }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onNewNote) { Icon(Icons.Default.Add, "New diary entry") } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (searchMode) {
                OutlinedTextField(query, viewModel::setQuery, Modifier.fillMaxWidth(), label = { Text("Search") }, singleLine = true)
                Spacer(Modifier.height(12.dp))
            }
            Text("Recent memories", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            if (notes.isEmpty()) Text("No diary entries yet. Tap + to write your first memory.", Modifier.padding(vertical = 24.dp))
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                items(notes, key = { it.id }) { note -> NoteCard(note, { onEditNote(note) }) }
            }
        }
    }
}

@Composable
private fun NoteCard(note: NoteEntity, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(18.dp)) {
            Text(note.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(note.content.ifBlank { "No content" }, style = MaterialTheme.typography.bodyMedium, maxLines = 4)
        }
    }
}
