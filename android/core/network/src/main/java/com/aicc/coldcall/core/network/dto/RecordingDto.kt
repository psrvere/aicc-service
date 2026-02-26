package com.aicc.coldcall.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecordingUploadResponseDto(
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class TranscribeRequestDto(
    @Json(name = "recording_url") val recordingUrl: String
)

@JsonClass(generateAdapter = true)
data class TranscriptionResponseDto(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class SummarizeRequestDto(
    @Json(name = "contact_id") val contactId: String,
    @Json(name = "transcript") val transcript: String
)

@JsonClass(generateAdapter = true)
data class AISummaryDto(
    @Json(name = "summary") val summary: String,
    @Json(name = "recommended_deal_stage") val recommendedDealStage: String,
    @Json(name = "next_action") val nextAction: String
)
