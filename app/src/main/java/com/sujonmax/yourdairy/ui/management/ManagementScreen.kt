@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.sujonmax.yourdairy.ui.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sujonmax.yourdairy.data.local.entity.FolderEntity
import com.sujonmax.yourdairy.data.local.entity.NoteEntity
import com.sujonmax.yourdairy.ui.diary.DiaryViewModel

@Composable
fun ManagementScreen(viewModel: DiaryViewModel, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val trash by viewModel.trash.collectAsStateWithLifecycle(initialValue = emptyList())
    val favorites by viewModel.favorites.collectAsStateWithLifecycle(initialValue = emptyList())
    val folders by viewModel.folders.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Diary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Favorites", "Folders", "Trash").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> FavoriteList(favorites, viewModel)
                1 -> FolderList(folders, viewModel)
                2 -> TrashList(trash, viewModel)
            }
        }
    }
}

@Composable
private fun FavoriteList(notes: List<NoteEntity>, viewModel: DiaryViewModel) {
    if (notes.isEmpty()) {
        Text("No favorite diaries yet.", Modifier.padding(20.dp))
        return
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                    Column(Modifier.weight(1f)) {
                        Text(note.title.ifBlank { "Untitled" })
                        Text(note.content.ifBlank { "No content" }, maxLines = 2)
                    }
                    IconButton(onClick = { viewModel.toggleFavorite(note) }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Remove favorite")
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderList(folders: List<FolderEntity>, viewModel: DiaryViewModel) {
    var showCreate by remember { mutableStateOf(false) }
    var folderName by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { showCreate = true }, Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Folder, contentDescription = null)
            Spacer(Modifier.padding(horizontal = 4.dp))
            Text("Create folder")
        }
        Spacer(Modifier.height(12.dp))
        if (folders.isEmpty()) {
            Text("No folders yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(folders, key = { it.id }) { folder ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(16.dp)) {
                            Icon(Icons.Default.Folder, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text(folder.name, Modifier.weight(1f))
                            IconButton(onClick = { viewModel.deleteFolder(folder) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete folder")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("New folder") },
            text = {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.createFolder(folderName)
                    folderName = ""
                    showCreate = false
                }, enabled = folderName.isNotBlank()) { Text("Create") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreate = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TrashList(notes: List<NoteEntity>, viewModel: DiaryViewModel) {
    Column(Modifier.fillMaxSize()) {
        if (notes.isNotEmpty()) {
            OutlinedButton(
                onClick = { viewModel.emptyTrash() },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Empty trash permanently")
            }
        }
        if (notes.isEmpty()) {
            Text("Trash is empty.", Modifier.padding(20.dp))
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(note.title.ifBlank { "Untitled" })
                                Text(note.content.ifBlank { "No content" }, maxLines = 2)
                            }
                            IconButton(onClick = { viewModel.restore(note.id) }) {
                                Icon(Icons.Default.RestoreFromTrash, contentDescription = "Restore")
                            }
                            IconButton(onClick = { viewModel.permanentlyDelete(note.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete permanently")
                            }
                        }
                    }
                }
            }
        }
    }
}
