package com.sujonmax.yourdairy

import android.app.Application
import com.sujonmax.yourdairy.data.DiaryRepository
import com.sujonmax.yourdairy.data.local.DreamDiaryDatabase

class DiaryApplication : Application() {
    val database: DreamDiaryDatabase by lazy { DreamDiaryDatabase.getInstance(this) }
    val repository: DiaryRepository by lazy {
        DiaryRepository(database.noteDao(), database.folderDao())
    }
}
