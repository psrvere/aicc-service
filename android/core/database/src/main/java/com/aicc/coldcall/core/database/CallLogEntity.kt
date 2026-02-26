package com.aicc.coldcall.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.model.Disposition

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "contact_id") val contactId: String,
    val timestamp: String,
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Int,
    val disposition: Disposition,
    val summary: String?,
    @ColumnInfo(name = "deal_stage") val dealStage: DealStage,
    @ColumnInfo(name = "recording_url") val recordingUrl: String?,
    val transcript: String?
)
