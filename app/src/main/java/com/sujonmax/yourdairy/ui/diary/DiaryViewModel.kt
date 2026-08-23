package com.sujonmax.yourdairy.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujonmax.yourdairy.data.DiaryRepository
import com.sujonmax.yourdairy.data.local.entity.NoteEntity
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class DiaryViewModel(
    private val repository: DiaryRepository
) : ViewModel() {
    val notes: Flow<List<NoteEntity>> = repository.notes
    val trash: Flow<List<NoteEntity>> = repository.trash
    val folders = repository.folders

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchResults: Flow<List<NoteEntity>> = _query.flatMapLatest { text ->
        if (text.isBlank()) repository.notes else repository.search(text)
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun saveNote(title: String, content: String, tags: String = "", folderId: Long? = null) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.saveNote(
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
            repository.saveNote(
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
