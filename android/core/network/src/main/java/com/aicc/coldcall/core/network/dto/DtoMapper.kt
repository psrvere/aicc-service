package com.aicc.coldcall.core.network.dto

import com.aicc.coldcall.core.model.AISummary
import com.aicc.coldcall.core.model.CallLog
import com.aicc.coldcall.core.model.CallPlanItem
import com.aicc.coldcall.core.model.Contact
import com.aicc.coldcall.core.model.DashboardStats
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.model.Disposition

inline fun <reified T : Enum<T>> safeValueOf(name: String, default: T): T =
    try {
        enumValueOf<T>(name)
    } catch (_: IllegalArgumentException) {
        default
    }

fun ContactDto.toDomain(): Contact = Contact(
    id = id,
    name = name,
    phone = phone,
    business = business,
    city = city,
    industry = industry,
    dealStage = safeValueOf(dealStage, DealStage.New),
    lastCalled = lastCalled,
    callCount = callCount,
    lastCallSummary = lastCallSummary,
    recordingLink = recordingLink,
    nextFollowUp = nextFollowUp,
    notes = notes,
    createdAt = createdAt
)

fun CallLogDto.toDomain(): CallLog = CallLog(
    id = id,
    contactId = contactId,
    timestamp = timestamp,
    durationSeconds = durationSeconds,
    disposition = safeValueOf(disposition, Disposition.NoAnswer),
    summary = summary,
    dealStage = safeValueOf(dealStage, DealStage.New),
    recordingUrl = recordingUrl,
    transcript = transcript
)

fun CallPlanItemDto.toDomain(): CallPlanItem = CallPlanItem(
    id = id,
    name = name,
    phone = phone,
    business = business,
    dealStage = safeValueOf(dealStage, DealStage.New),
    nextFollowUp = nextFollowUp,
    callCount = callCount,
    lastCallSummary = lastCallSummary,
    reason = reason
)

fun DashboardStatsDto.toDomain(): DashboardStats = DashboardStats(
    callsToday = callsToday,
    connectedToday = connectedToday,
    conversionRate = conversionRate,
    streak = streak,
    pipeline = pipeline.mapNotNull { (key, value) ->
        try {
            DealStage.valueOf(key) to value
        } catch (_: IllegalArgumentException) {
            null
        }
    }.toMap()
)

fun AISummaryDto.toDomain(): AISummary = AISummary(
    summary = summary,
    recommendedDealStage = safeValueOf(recommendedDealStage, DealStage.New),
    nextAction = nextAction
)
