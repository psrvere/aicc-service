package com.aicc.coldcall.core.model

data class CallPlanItem(
    val id: String,
    val name: String,
    val phone: String,
    val business: String? = null,
    val dealStage: DealStage,
    val nextFollowUp: String? = null,
    val callCount: Int,
    val lastCallSummary: String? = null,
    val reason: String
)
