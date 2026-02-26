package com.aicc.coldcall.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "business") val business: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "industry") val industry: String? = null,
    @Json(name = "deal_stage") val dealStage: String = "New",
    @Json(name = "last_called") val lastCalled: String? = null,
    @Json(name = "call_count") val callCount: Int = 0,
    @Json(name = "last_call_summary") val lastCallSummary: String? = null,
    @Json(name = "recording_link") val recordingLink: String? = null,
    @Json(name = "next_follow_up") val nextFollowUp: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ContactCreateDto(
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "business") val business: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "industry") val industry: String? = null,
    @Json(name = "deal_stage") val dealStage: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "next_follow_up") val nextFollowUp: String? = null
)

@JsonClass(generateAdapter = true)
data class ContactUpdateDto(
    @Json(name = "name") val name: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "business") val business: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "industry") val industry: String? = null,
    @Json(name = "deal_stage") val dealStage: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "next_follow_up") val nextFollowUp: String? = null,
    @Json(name = "last_call_summary") val lastCallSummary: String? = null,
    @Json(name = "recording_link") val recordingLink: String? = null
)
