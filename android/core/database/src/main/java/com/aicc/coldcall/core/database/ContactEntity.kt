package com.aicc.coldcall.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aicc.coldcall.core.model.DealStage

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val business: String?,
    val city: String?,
    val industry: String?,
    @ColumnInfo(name = "deal_stage") val dealStage: DealStage,
    @ColumnInfo(name = "last_called") val lastCalled: String?,
    @ColumnInfo(name = "call_count") val callCount: Int,
    @ColumnInfo(name = "last_call_summary") val lastCallSummary: String?,
    @ColumnInfo(name = "recording_link") val recordingLink: String?,
    @ColumnInfo(name = "next_follow_up") val nextFollowUp: String?,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: String?
)
