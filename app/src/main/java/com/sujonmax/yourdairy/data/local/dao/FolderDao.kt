package com.sujonmax.yourdairy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sujonmax.yourdairy.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name COLLATE NOCASE ASC")
    fun observeFolders(): Flow<List<FolderEntity>>

    @Insert
    suspend fun insert(folder: FolderEntity): Long

    @Update
    suspend fun update(folder: FolderEntity)

    @Delete
    suspend fun delete(folder: FolderEntity)
}
