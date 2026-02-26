package com.aicc.coldcall.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DashboardStatsDto(
    @Json(name = "calls_today") val callsToday: Int,
    @Json(name = "connected_today") val connectedToday: Int,
    @Json(name = "conversion_rate") val conversionRate: Double,
    @Json(name = "streak") val streak: Int,
    @Json(name = "pipeline") val pipeline: Map<String, Int>
)
