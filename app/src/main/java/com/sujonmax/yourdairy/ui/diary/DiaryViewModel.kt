package com.sujonmax.yourdairy.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujonmax.yourdairy.data.local.entity.NoteEntity
import com.sujonmax.yourdairy.data.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiaryViewModel(
    private val repository: DiaryRepository
) : ViewModel() {
    val notes = repository.observeNotes()
    val trash = repository.observeTrash()
    val folders = repository.observeFolders()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchResults = kotlinx.coroutines.flow.flatMapLatest(_query) { text ->
        if (text.isBlank()) repository.observeNotes() else repository.search(text.trim())
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun saveNote(title: String, content: String, tags: String = "", folderId: Long? = null) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.insertNote(
                NoteEntity(
                    title = title.trim(),
                    content = content,
                    tags = tags.trim(),
                    folderId = folderId,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun updateNote(note: NoteEntity, title: String, content: String, tags: String = "", folderId: Long? = null) {
        viewModelScope.launch {
            repository.updateNote(
                note.copy(
                    title = title.trim(),
                    content = content,
                    tags = tags.trim(),
                    folderId = folderId,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun moveToTrash(id: Long) = viewModelScope.launch { repository.moveToTrash(id) }
    fun restore(id: Long) = viewModelScope.launch { repository.restore(id) }
    fun permanentlyDelete(id: Long) = viewModelScope.launch { repository.permanentlyDelete(id) }
}
