package com.aicc.coldcall.core.model

data class ContactCreate(
    val name: String,
    val phone: String,
    val business: String? = null,
    val city: String? = null,
    val industry: String? = null,
    val dealStage: DealStage? = null,
    val notes: String? = null,
    val nextFollowUp: String? = null,
)

data class ContactUpdate(
    val name: String? = null,
    val phone: String? = null,
    val business: String? = null,
    val city: String? = null,
    val industry: String? = null,
    val dealStage: DealStage? = null,
    val notes: String? = null,
    val nextFollowUp: String? = null,
    val lastCallSummary: String? = null,
    val recordingLink: String? = null,
)
