package com.sujonmax.yourdairy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sujonmax.yourdairy.data.local.dao.AttachmentDao
import com.sujonmax.yourdairy.data.local.dao.FolderDao
import com.sujonmax.yourdairy.data.local.dao.NoteDao
import com.sujonmax.yourdairy.data.local.entity.AttachmentEntity
import com.sujonmax.yourdairy.data.local.entity.FolderEntity
import com.sujonmax.yourdairy.data.local.entity.NoteEntity

@Database(
    entities = [NoteEntity::class, FolderEntity::class, AttachmentEntity::class],
    version = 3,
    exportSchema = false
)
abstract class DreamDiaryDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS attachments (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, noteId INTEGER NOT NULL, uri TEXT NOT NULL, type TEXT NOT NULL, displayName TEXT, createdAt INTEGER NOT NULL)"
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_attachments_noteId ON attachments(noteId)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN backgroundKey TEXT NOT NULL DEFAULT 'default'")
            }
        }

        @Volatile
        private var INSTANCE: DreamDiaryDatabase? = null

        fun getInstance(context: Context): DreamDiaryDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DreamDiaryDatabase::class.java,
                    "dream_diary.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
