package com.aicc.coldcall.core.database

import com.aicc.coldcall.core.model.Contact
import com.aicc.coldcall.core.model.DealStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContactEntityTest {

    @Test
    fun `entity fields map to Contact domain model`() {
        val entity = ContactEntity(
            id = "c1",
            name = "Alice Smith",
            phone = "5551234567",
            business = "Acme Corp",
            city = "Austin",
            industry = "Tech",
            dealStage = DealStage.Qualified,
            lastCalled = "2025-01-15T10:30:00",
            callCount = 3,
            lastCallSummary = "Discussed pricing",
            recordingLink = "https://storage.example.com/rec1.m4a",
            nextFollowUp = "2025-01-20",
            notes = "Interested in premium plan",
            createdAt = "2025-01-01T09:00:00"
        )

        val domain = entity.toDomain()

        assertEquals("c1", domain.id)
        assertEquals("Alice Smith", domain.name)
        assertEquals("5551234567", domain.phone)
        assertEquals("Acme Corp", domain.business)
        assertEquals("Austin", domain.city)
        assertEquals("Tech", domain.industry)
        assertEquals(DealStage.Qualified, domain.dealStage)
        assertEquals("2025-01-15T10:30:00", domain.lastCalled)
        assertEquals(3, domain.callCount)
        assertEquals("Discussed pricing", domain.lastCallSummary)
        assertEquals("https://storage.example.com/rec1.m4a", domain.recordingLink)
        assertEquals("2025-01-20", domain.nextFollowUp)
        assertEquals("Interested in premium plan", domain.notes)
        assertEquals("2025-01-01T09:00:00", domain.createdAt)
    }

    @Test
    fun `domain Contact maps to entity`() {
        val contact = Contact(
            id = "c2",
            name = "Bob Jones",
            phone = "5559876543",
            dealStage = DealStage.New,
            callCount = 0
        )

        val entity = contact.toEntity()

        assertEquals("c2", entity.id)
        assertEquals("Bob Jones", entity.name)
        assertEquals("5559876543", entity.phone)
        assertNull(entity.business)
        assertNull(entity.city)
        assertNull(entity.industry)
        assertEquals(DealStage.New, entity.dealStage)
        assertNull(entity.lastCalled)
        assertEquals(0, entity.callCount)
        assertNull(entity.lastCallSummary)
        assertNull(entity.recordingLink)
        assertNull(entity.nextFollowUp)
        assertNull(entity.notes)
        assertNull(entity.createdAt)
    }

    @Test
    fun `round-trip entity to domain and back preserves all fields`() {
        val original = ContactEntity(
            id = "c3",
            name = "Carol White",
            phone = "5550001111",
            business = "White Industries",
            city = "Denver",
            industry = "Manufacturing",
            dealStage = DealStage.Proposal,
            lastCalled = "2025-02-01T14:00:00",
            callCount = 5,
            lastCallSummary = "Sent proposal",
            recordingLink = "https://storage.example.com/rec3.m4a",
            nextFollowUp = "2025-02-10",
            notes = "Decision maker",
            createdAt = "2024-12-01T08:00:00"
        )

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original, roundTripped)
    }
}
