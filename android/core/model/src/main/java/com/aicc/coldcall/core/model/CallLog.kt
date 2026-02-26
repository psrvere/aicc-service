package com.aicc.coldcall.core.model

data class CallLog(
    val id: String,
    val contactId: String,
    val timestamp: String,
    val durationSeconds: Int,
    val disposition: Disposition,
    val summary: String? = null,
    val dealStage: DealStage,
    val recordingUrl: String? = null,
    val transcript: String? = null
)
