package com.aicc.coldcall.core.model

data class Contact(
    val id: String,
    val name: String,
    val phone: String,
    val business: String? = null,
    val city: String? = null,
    val industry: String? = null,
    val dealStage: DealStage = DealStage.New,
    val lastCalled: String? = null,
    val callCount: Int = 0,
    val lastCallSummary: String? = null,
    val recordingLink: String? = null,
    val nextFollowUp: String? = null,
    val notes: String? = null,
    val createdAt: String? = null
)
