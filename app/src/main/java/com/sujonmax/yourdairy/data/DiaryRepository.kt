package com.sujonmax.yourdairy.data

import com.sujonmax.yourdairy.data.local.dao.FolderDao
import com.sujonmax.yourdairy.data.local.dao.NoteDao
import com.sujonmax.yourdairy.data.local.entity.FolderEntity
import com.sujonmax.yourdairy.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class DiaryRepository(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao
) {
    val notes: Flow<List<NoteEntity>> = noteDao.observeActiveNotes()
    val trash: Flow<List<NoteEntity>> = noteDao.observeTrash()
    val favorites: Flow<List<NoteEntity>> = noteDao.observeFavorites()
    val folders: Flow<List<FolderEntity>> = folderDao.observeFolders()

    fun search(query: String): Flow<List<NoteEntity>> = noteDao.search(query.trim())

    suspend fun saveNote(note: NoteEntity): Long {
        return if (note.id == 0L) {
            noteDao.insert(note)
        } else {
            noteDao.update(note.copy(updatedAt = System.currentTimeMillis()))
            note.id
        }
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) = noteDao.setFavorite(id, favorite)

    suspend fun createFolder(name: String): Long =
        folderDao.insert(FolderEntity(name = name.trim()))

    suspend fun renameFolder(folder: FolderEntity, newName: String) =
        folderDao.update(folder.copy(name = newName.trim()))

    suspend fun deleteFolder(folder: FolderEntity) = folderDao.delete(folder)

    suspend fun moveToTrash(id: Long) = noteDao.moveToTrash(id)
    suspend fun restore(id: Long) = noteDao.restore(id)
    suspend fun permanentlyDelete(id: Long) = noteDao.permanentlyDelete(id)
    suspend fun emptyTrash() = noteDao.emptyTrash()
}
