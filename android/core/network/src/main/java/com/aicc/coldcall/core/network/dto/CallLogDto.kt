package com.aicc.coldcall.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CallLogDto(
    @Json(name = "id") val id: String,
    @Json(name = "contact_id") val contactId: String,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "duration_seconds") val durationSeconds: Int,
    @Json(name = "disposition") val disposition: String,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "deal_stage") val dealStage: String,
    @Json(name = "recording_url") val recordingUrl: String? = null,
    @Json(name = "transcript") val transcript: String? = null
)

@JsonClass(generateAdapter = true)
data class CallLogCreateDto(
    @Json(name = "contact_id") val contactId: String,
    @Json(name = "duration_seconds") val durationSeconds: Int,
    @Json(name = "disposition") val disposition: String,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "deal_stage") val dealStage: String? = null,
    @Json(name = "recording_url") val recordingUrl: String? = null,
    @Json(name = "transcript") val transcript: String? = null,
    @Json(name = "next_follow_up") val nextFollowUp: String? = null
)
