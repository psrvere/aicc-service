package com.aicc.coldcall.core.database

import com.aicc.coldcall.core.model.CallLog
import com.aicc.coldcall.core.model.Contact

fun ContactEntity.toDomain(): Contact = Contact(
    id = id,
    name = name,
    phone = phone,
    business = business,
    city = city,
    industry = industry,
    dealStage = dealStage,
    lastCalled = lastCalled,
    callCount = callCount,
    lastCallSummary = lastCallSummary,
    recordingLink = recordingLink,
    nextFollowUp = nextFollowUp,
    notes = notes,
    createdAt = createdAt
)

fun Contact.toEntity(): ContactEntity = ContactEntity(
    id = id,
    name = name,
    phone = phone,
    business = business,
    city = city,
    industry = industry,
    dealStage = dealStage,
    lastCalled = lastCalled,
    callCount = callCount,
    lastCallSummary = lastCallSummary,
    recordingLink = recordingLink,
    nextFollowUp = nextFollowUp,
    notes = notes,
    createdAt = createdAt
)

fun CallLogEntity.toDomain(): CallLog = CallLog(
    id = id,
    contactId = contactId,
    timestamp = timestamp,
    durationSeconds = durationSeconds,
    disposition = disposition,
    summary = summary,
    dealStage = dealStage,
    recordingUrl = recordingUrl,
    transcript = transcript
)

fun CallLog.toEntity(): CallLogEntity = CallLogEntity(
    id = id,
    contactId = contactId,
    timestamp = timestamp,
    durationSeconds = durationSeconds,
    disposition = disposition,
    summary = summary,
    dealStage = dealStage,
    recordingUrl = recordingUrl,
    transcript = transcript
)
