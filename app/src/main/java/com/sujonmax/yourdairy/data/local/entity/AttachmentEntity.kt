package com.sujonmax.yourdairy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attachments")
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val uri: String,
    val type: String,
    val displayName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
