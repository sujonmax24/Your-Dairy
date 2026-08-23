package com.sujonmax.yourdairy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sujonmax.yourdairy.data.local.dao.FolderDao
import com.sujonmax.yourdairy.data.local.dao.NoteDao
import com.sujonmax.yourdairy.data.local.entity.FolderEntity
import com.sujonmax.yourdairy.data.local.entity.NoteEntity

@Database(
    entities = [NoteEntity::class, FolderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DreamDiaryDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: DreamDiaryDatabase? = null

        fun getInstance(context: Context): DreamDiaryDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DreamDiaryDatabase::class.java,
                    "dream_diary.db"
                ).build().also { INSTANCE = it }
            }
    }
}
