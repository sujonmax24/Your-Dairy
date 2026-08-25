package com.sujonmax.yourdairy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sujonmax.yourdairy.data.local.entity.NoteEntity
import com.sujonmax.yourdairy.security.SecurityManager
import com.sujonmax.yourdairy.ui.about.AboutScreen
import com.sujonmax.yourdairy.ui.diary.DiaryEditorScreen
import com.sujonmax.yourdairy.ui.diary.DiaryViewModel
import com.sujonmax.yourdairy.ui.diary.DiaryViewModelFactory
import com.sujonmax.yourdairy.ui.management.ManagementScreen
import com.sujonmax.yourdairy.ui.settings.SettingsScreen
import com.sujonmax.yourdairy.ui.theme.YourDairyTheme
import com.sujonmax.yourdairy.ui.txt.TxtEditorScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val application = application as DiaryApplication
            val viewModel: DiaryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = DiaryViewModelFactory(application.repository))
            val prefs = remember { getSharedPreferences("dream_diary_settings", MODE_PRIVATE) }
            val security = remember { SecurityManager(this) }
            var dark by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
            var themeMode by remember { mutableStateOf(prefs.getString("theme_mode", "system") ?: "system") }
            var fontScale by remember { mutableFloatStateOf(prefs.getFloat("font_scale", 1f)) }
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val useDark = when (themeMode) { "dark" -> true; "light" -> false; else -> systemDark }

            YourDairyTheme(darkTheme = useDark, fontScale = fontScale) {
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
                            viewModel.saveNote(note)
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
                title = { Column { Text("Dream Diary", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold); Text("create by sujonmax", style = MaterialTheme.typography.labelSmall) } },
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(note.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(note.mood?.take(2).orEmpty(), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(6.dp))
            Text(note.content.ifBlank { "No content" }, style = MaterialTheme.typography.bodyMedium, maxLines = 4)
            if (note.location != null) { Spacer(Modifier.height(4.dp)); Text("📍 ${note.location}", style = MaterialTheme.typography.labelMedium) }
            if (note.imageUris.isNotBlank() || note.audioUris.isNotBlank()) { Spacer(Modifier.height(4.dp)); Text("📎 Memory attachments", style = MaterialTheme.typography.labelMedium) }
            if (note.isFavorite) { Spacer(Modifier.height(6.dp)); Text("★ Favorite", style = MaterialTheme.typography.labelMedium) }
        }
    }
}
