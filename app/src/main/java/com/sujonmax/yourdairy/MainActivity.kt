@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import com.sujonmax.yourdairy.data.local.entity.NoteEntity
import com.sujonmax.yourdairy.security.BiometricHelper
import com.sujonmax.yourdairy.security.SecurityManager
import com.sujonmax.yourdairy.ui.about.AboutScreen
import com.sujonmax.yourdairy.ui.diary.DiaryEditorScreen
import com.sujonmax.yourdairy.ui.diary.DiaryViewModel
import com.sujonmax.yourdairy.ui.diary.DiaryViewModelFactory
import com.sujonmax.yourdairy.ui.management.ManagementScreen
import com.sujonmax.yourdairy.ui.security.PinLockScreen
import com.sujonmax.yourdairy.ui.security.PinSetupScreen
import com.sujonmax.yourdairy.ui.security.RecoveryScreen
import com.sujonmax.yourdairy.ui.settings.SettingsScreen
import com.sujonmax.yourdairy.ui.theme.YourDairyTheme
import com.sujonmax.yourdairy.ui.txt.TxtEditorScreen

class MainActivity : FragmentActivity() {
    private val diaryViewModel: DiaryViewModel by viewModels { DiaryViewModelFactory((application as DiaryApplication).repository) }
    private lateinit var security: SecurityManager
    private var unlocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        security = SecurityManager(applicationContext)
        setContent { SecurityGate() }
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
            recoveryMode -> RecoveryScreen(security, onRecovered = { recoveryMode = false; unlocked = true }, onCancel = { recoveryMode = false })
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
        val prefs = getSharedPreferences("dream_diary_settings", MODE_PRIVATE)
        var themeMode by rememberSaveable { mutableStateOf(prefs.getString("theme_mode", "system") ?: "system") }
        var fontScale by rememberSaveable { mutableStateOf(prefs.getFloat("font_scale", 1f)) }
        val dark = when (themeMode) {
            "dark" -> true
            "light" -> false
            else -> androidx.compose.foundation.isSystemInDarkTheme()
        }
        YourDairyTheme(darkTheme = dark, fontScale = fontScale) {
            var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
            var isEditorOpen by rememberSaveable { mutableStateOf(false) }
            var isAboutOpen by rememberSaveable { mutableStateOf(false) }
            var isManagementOpen by rememberSaveable { mutableStateOf(false) }
            var isTxtEditorOpen by rememberSaveable { mutableStateOf(false) }
            var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
            val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList())

            when {
                isSettingsOpen -> SettingsScreen(
                    security = security,
                    themeMode = themeMode,
                    onThemeModeChange = { themeMode = it; prefs.edit().putString("theme_mode", it).apply() },
                    fontScale = fontScale,
                    onFontScaleChange = { fontScale = it; prefs.edit().putFloat("font_scale", it).apply() },
                    onBack = { isSettingsOpen = false },
                    onAbout = { isSettingsOpen = false; isAboutOpen = true }
                )
                isAboutOpen -> AboutScreen(onBack = { isAboutOpen = false })
                isManagementOpen -> ManagementScreen(viewModel = viewModel, onBack = { isManagementOpen = false })
                isTxtEditorOpen -> TxtEditorScreen(onBack = { isTxtEditorOpen = false })
                isEditorOpen -> DiaryEditorScreen(
                    note = editingNote,
                    folders = folders,
                    onBack = { isEditorOpen = false },
                    onSave = { note ->
                        val existing = editingNote
                        if (existing == null) viewModel.saveNote(note.title, note.content, note.tags, note.folderId)
                        else viewModel.updateNote(existing, note.title, note.content, note.tags, note.folderId)
                        isEditorOpen = false
                    }
                )
                else -> DreamDiaryHome(
                    viewModel = viewModel,
                    onNewNote = { editingNote = null; isEditorOpen = true },
                    onEditNote = { note -> editingNote = note; isEditorOpen = true },
                    onAbout = { isAboutOpen = true },
                    onManagement = { isManagementOpen = true },
                    onTxtEditor = { isTxtEditorOpen = true },
                    onSettings = { isSettingsOpen = true }
                )
            }
        }
    }
}

@Composable
private fun DreamDiaryHome(
    viewModel: DiaryViewModel,
    onNewNote: () -> Unit,
    onEditNote: (NoteEntity) -> Unit,
    onAbout: () -> Unit,
    onManagement: () -> Unit,
    onTxtEditor: () -> Unit,
    onSettings: () -> Unit
) {
    val notes by viewModel.searchResults.collectAsStateWithLifecycle(initialValue = emptyList())
    val query by viewModel.query.collectAsStateWithLifecycle()
    var searchMode by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Dream Diary", fontWeight = FontWeight.Bold); Text("create by sujonmax", style = MaterialTheme.typography.labelSmall) } },
                actions = {
                    IconButton(onClick = onManagement) { Icon(Icons.Default.Favorite, contentDescription = "Favorites, folders and trash") }
                    IconButton(onClick = onTxtEditor) { Icon(Icons.Default.Description, contentDescription = "TXT editor") }
                    IconButton(onClick = { searchMode = !searchMode }) { Icon(Icons.Default.Search, contentDescription = "Search diary") }
                    IconButton(onClick = onAbout) { Icon(Icons.Default.Info, contentDescription = "About Dream Diary") }
                    IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, contentDescription = "Settings") }
                }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onNewNote) { Icon(Icons.Default.Add, contentDescription = "New diary entry") } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (searchMode) {
                OutlinedTextField(value = query, onValueChange = viewModel::setQuery, modifier = Modifier.fillMaxWidth(), label = { Text("Search title, diary content or tags") }, singleLine = true)
                Spacer(Modifier.height(12.dp))
            }
            Text("Recent memories", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            if (notes.isEmpty()) Text("No diary entries yet. Tap + to write your first memory.", Modifier.padding(vertical = 24.dp))
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) { items(notes, key = { it.id }) { note -> NoteCard(note, onEditNote) } }
        }
    }
}

@Composable
private fun NoteCard(note: NoteEntity, onClick: (NoteEntity) -> Unit) {
    Card(onClick = { onClick(note) }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Text(note.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(note.content.ifBlank { "No content" }, style = MaterialTheme.typography.bodyMedium, maxLines = 4)
            if (note.isFavorite) { Spacer(Modifier.height(6.dp)); Text("★ Favorite", style = MaterialTheme.typography.labelMedium) }
        }
    }
}
