package com.aicc.coldcall.core.network

import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.model.Disposition
import com.aicc.coldcall.core.network.dto.AISummaryDto
import com.aicc.coldcall.core.network.dto.CallLogDto
import com.aicc.coldcall.core.network.dto.CallPlanItemDto
import com.aicc.coldcall.core.network.dto.ContactDto
import com.aicc.coldcall.core.network.dto.DashboardStatsDto
import com.aicc.coldcall.core.network.dto.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DtoMapperTest {

    @Test
    fun `ContactDto maps to Contact domain model`() {
        val dto = ContactDto(
            id = "uuid-1",
            name = "John",
            phone = "+123",
            business = "Acme",
            city = "Mumbai",
            industry = "Tech",
            dealStage = "Qualified",
            lastCalled = "2026-02-25T10:30:00",
            callCount = 3,
            lastCallSummary = "Discussed pricing",
            recordingLink = "https://example.com/rec.m4a",
            nextFollowUp = "2026-03-01",
            notes = "Notes",
            createdAt = "2026-01-15T09:00:00"
        )

        val contact = dto.toDomain()

        assertEquals("uuid-1", contact.id)
        assertEquals("John", contact.name)
        assertEquals(DealStage.Qualified, contact.dealStage)
        assertEquals(3, contact.callCount)
        assertEquals("2026-02-25T10:30:00", contact.lastCalled)
    }

    @Test
    fun `ContactDto with null fields maps correctly`() {
        val dto = ContactDto(
            id = "uuid-2",
            name = "Jane",
            phone = "+456",
            dealStage = "New",
            callCount = 0,
            createdAt = "2026-01-15T09:00:00"
        )

        val contact = dto.toDomain()

        assertNull(contact.business)
        assertNull(contact.city)
        assertEquals(DealStage.New, contact.dealStage)
        assertEquals(0, contact.callCount)
    }

    @Test
    fun `CallLogDto maps to CallLog domain model`() {
        val dto = CallLogDto(
            id = "log-1",
            contactId = "c-1",
            timestamp = "2026-02-25T14:30:00",
            durationSeconds = 180,
            disposition = "Connected",
            summary = "Good call",
            dealStage = "Proposal",
            recordingUrl = "https://example.com/rec.m4a",
            transcript = "Hello..."
        )

        val log = dto.toDomain()

        assertEquals("log-1", log.id)
        assertEquals("c-1", log.contactId)
        assertEquals(Disposition.Connected, log.disposition)
        assertEquals(DealStage.Proposal, log.dealStage)
        assertEquals(180, log.durationSeconds)
    }

    @Test
    fun `CallPlanItemDto maps to CallPlanItem domain model`() {
        val dto = CallPlanItemDto(
            id = "item-1",
            name = "Lead",
            phone = "+111",
            business = "Corp",
            dealStage = "New",
            nextFollowUp = null,
            callCount = 0,
            lastCallSummary = null,
            reason = "New contact"
        )

        val item = dto.toDomain()

        assertEquals("item-1", item.id)
        assertEquals(DealStage.New, item.dealStage)
        assertEquals("New contact", item.reason)
    }

    @Test
    fun `DashboardStatsDto maps to DashboardStats domain model`() {
        val dto = DashboardStatsDto(
            callsToday = 10,
            connectedToday = 5,
            conversionRate = 0.5,
            streak = 3,
            pipeline = mapOf("New" to 10, "Won" to 4)
        )

        val stats = dto.toDomain()

        assertEquals(10, stats.callsToday)
        assertEquals(5, stats.connectedToday)
        assertEquals(0.5, stats.conversionRate, 0.001)
        assertEquals(3, stats.streak)
        assertEquals(10, stats.pipeline[DealStage.New])
        assertEquals(4, stats.pipeline[DealStage.Won])
    }

    @Test
    fun `DashboardStatsDto with unknown pipeline key ignores it`() {
        val dto = DashboardStatsDto(
            callsToday = 0,
            connectedToday = 0,
            conversionRate = 0.0,
            streak = 0,
            pipeline = mapOf("New" to 5, "UnknownStage" to 3)
        )

        val stats = dto.toDomain()

        assertEquals(1, stats.pipeline.size)
        assertEquals(5, stats.pipeline[DealStage.New])
    }

    @Test
    fun `AISummaryDto maps to AISummary domain model`() {
        val dto = AISummaryDto(
            summary = "Good conversation",
            recommendedDealStage = "Proposal",
            nextAction = "Send proposal"
        )

        val summary = dto.toDomain()

        assertEquals("Good conversation", summary.summary)
        assertEquals(DealStage.Proposal, summary.recommendedDealStage)
        assertEquals("Send proposal", summary.nextAction)
    }
}
