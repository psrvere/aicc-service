package com.aicc.coldcall.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "contact_id") val contactId: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "remote_url") val remoteUrl: String? = null,
    @ColumnInfo(name = "is_uploaded") val isUploaded: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
