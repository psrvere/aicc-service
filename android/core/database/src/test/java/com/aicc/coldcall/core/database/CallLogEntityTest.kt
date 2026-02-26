package com.aicc.coldcall.core.database

import com.aicc.coldcall.core.model.CallLog
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.model.Disposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CallLogEntityTest {

    @Test
    fun `entity fields map to CallLog domain model`() {
        val entity = CallLogEntity(
            id = "log1",
            contactId = "c1",
            timestamp = "2025-01-15T10:30:00",
            durationSeconds = 180,
            disposition = Disposition.Connected,
            summary = "Great conversation about product",
            dealStage = DealStage.Qualified,
            recordingUrl = "https://storage.example.com/rec1.m4a",
            transcript = "Hello, this is Alice..."
        )

        val domain = entity.toDomain()

        assertEquals("log1", domain.id)
        assertEquals("c1", domain.contactId)
        assertEquals("2025-01-15T10:30:00", domain.timestamp)
        assertEquals(180, domain.durationSeconds)
        assertEquals(Disposition.Connected, domain.disposition)
        assertEquals("Great conversation about product", domain.summary)
        assertEquals(DealStage.Qualified, domain.dealStage)
        assertEquals("https://storage.example.com/rec1.m4a", domain.recordingUrl)
        assertEquals("Hello, this is Alice...", domain.transcript)
    }

    @Test
    fun `domain CallLog maps to entity`() {
        val callLog = CallLog(
            id = "log2",
            contactId = "c2",
            timestamp = "2025-01-16T09:00:00",
            durationSeconds = 0,
            disposition = Disposition.NoAnswer,
            dealStage = DealStage.Contacted
        )

        val entity = callLog.toEntity()

        assertEquals("log2", entity.id)
        assertEquals("c2", entity.contactId)
        assertEquals("2025-01-16T09:00:00", entity.timestamp)
        assertEquals(0, entity.durationSeconds)
        assertEquals(Disposition.NoAnswer, entity.disposition)
        assertNull(entity.summary)
        assertEquals(DealStage.Contacted, entity.dealStage)
        assertNull(entity.recordingUrl)
        assertNull(entity.transcript)
    }

    @Test
    fun `round-trip entity to domain and back preserves all fields`() {
        val original = CallLogEntity(
            id = "log3",
            contactId = "c3",
            timestamp = "2025-02-01T14:00:00",
            durationSeconds = 300,
            disposition = Disposition.Callback,
            summary = "Will call back tomorrow",
            dealStage = DealStage.Negotiation,
            recordingUrl = "https://storage.example.com/rec3.m4a",
            transcript = "Detailed transcript here..."
        )

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `all disposition types map correctly`() {
        Disposition.entries.forEach { disposition ->
            val entity = CallLogEntity(
                id = "test",
                contactId = "c1",
                timestamp = "2025-01-01T00:00:00",
                durationSeconds = 0,
                disposition = disposition,
                summary = null,
                dealStage = DealStage.New,
                recordingUrl = null,
                transcript = null
            )
            assertEquals(disposition, entity.toDomain().disposition)
        }
    }

    @Test
    fun `all deal stage types map correctly`() {
        DealStage.entries.forEach { stage ->
            val entity = CallLogEntity(
                id = "test",
                contactId = "c1",
                timestamp = "2025-01-01T00:00:00",
                durationSeconds = 0,
                disposition = Disposition.Connected,
                summary = null,
                dealStage = stage,
                recordingUrl = null,
                transcript = null
            )
            assertEquals(stage, entity.toDomain().dealStage)
        }
    }
}
