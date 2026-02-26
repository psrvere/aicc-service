package com.aicc.coldcall.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CallLogTest {
    @Test
    fun `create call log with all fields`() {
        val log = CallLog(
            id = "log-1",
            contactId = "contact-1",
            timestamp = "2026-02-25T14:30:00",
            durationSeconds = 180,
            disposition = Disposition.Connected,
            summary = "Good conversation about deal",
            dealStage = DealStage.Proposal,
            recordingUrl = "https://example.com/rec.m4a",
            transcript = "Hello, I wanted to discuss..."
        )

        assertEquals("log-1", log.id)
        assertEquals("contact-1", log.contactId)
        assertEquals("2026-02-25T14:30:00", log.timestamp)
        assertEquals(180, log.durationSeconds)
        assertEquals(Disposition.Connected, log.disposition)
        assertEquals("Good conversation about deal", log.summary)
        assertEquals(DealStage.Proposal, log.dealStage)
        assertEquals("https://example.com/rec.m4a", log.recordingUrl)
        assertEquals("Hello, I wanted to discuss...", log.transcript)
    }

    @Test
    fun `create call log with minimal fields`() {
        val log = CallLog(
            id = "log-2",
            contactId = "contact-2",
            timestamp = "2026-02-25T15:00:00",
            durationSeconds = 0,
            disposition = Disposition.NoAnswer,
            dealStage = DealStage.New
        )

        assertEquals("log-2", log.id)
        assertEquals(Disposition.NoAnswer, log.disposition)
        assertNull(log.summary)
        assertNull(log.recordingUrl)
        assertNull(log.transcript)
    }

    @Test
    fun `call log maps all disposition types`() {
        Disposition.values().forEach { disp ->
            val log = CallLog(
                id = "id",
                contactId = "cid",
                timestamp = "2026-01-01T00:00:00",
                durationSeconds = 0,
                disposition = disp,
                dealStage = DealStage.New
            )
            assertEquals(disp, log.disposition)
        }
    }
}
