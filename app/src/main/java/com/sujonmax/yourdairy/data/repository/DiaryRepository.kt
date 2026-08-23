package com.sujonmax.yourdairy.data.repository

import com.sujonmax.yourdairy.data.local.dao.FolderDao
import com.sujonmax.yourdairy.data.local.dao.NoteDao
import com.sujonmax.yourdairy.data.local.entity.FolderEntity
import com.sujonmax.yourdairy.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class DiaryRepository(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao
) {
    fun observeNotes(): Flow<List<NoteEntity>> = noteDao.observeActiveNotes()
    fun observeTrash(): Flow<List<NoteEntity>> = noteDao.observeTrash()
    fun observeNote(id: Long): Flow<NoteEntity?> = noteDao.observeById(id)
    fun search(query: String): Flow<List<NoteEntity>> = noteDao.search(query)
    fun observeFolders(): Flow<List<FolderEntity>> = folderDao.observeFolders()

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insert(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.update(note)
    suspend fun moveToTrash(id: Long) = noteDao.moveToTrash(id)
    suspend fun restore(id: Long) = noteDao.restore(id)
    suspend fun permanentlyDelete(id: Long) = noteDao.permanentlyDelete(id)

    suspend fun insertFolder(folder: FolderEntity): Long = folderDao.insert(folder)
    suspend fun updateFolder(folder: FolderEntity) = folderDao.update(folder)
    suspend fun deleteFolder(folder: FolderEntity) = folderDao.delete(folder)
}
