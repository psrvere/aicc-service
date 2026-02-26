package com.aicc.coldcall.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CallPlanItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "business") val business: String? = null,
    @Json(name = "deal_stage") val dealStage: String,
    @Json(name = "next_follow_up") val nextFollowUp: String? = null,
    @Json(name = "call_count") val callCount: Int,
    @Json(name = "last_call_summary") val lastCallSummary: String? = null,
    @Json(name = "reason") val reason: String
)
