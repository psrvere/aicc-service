package com.aicc.coldcall.core.model

data class AISummary(
    val summary: String,
    val recommendedDealStage: DealStage,
    val nextAction: String
)
