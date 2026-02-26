package com.aicc.coldcall.core.model

data class DashboardStats(
    val callsToday: Int = 0,
    val connectedToday: Int = 0,
    val conversionRate: Double = 0.0,
    val streak: Int = 0,
    val pipeline: Map<DealStage, Int> = emptyMap()
)
