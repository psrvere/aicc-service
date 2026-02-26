package com.aicc.coldcall.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContactTest {
    @Test
    fun `create contact with all fields`() {
        val contact = Contact(
            id = "uuid-123",
            name = "John Doe",
            phone = "+1234567890",
            business = "Acme Corp",
            city = "Mumbai",
            industry = "Tech",
            dealStage = DealStage.Qualified,
            lastCalled = "2026-02-25T10:30:00",
            callCount = 3,
            lastCallSummary = "Discussed pricing",
            recordingLink = "https://example.com/rec.m4a",
            nextFollowUp = "2026-03-01",
            notes = "Interested in premium plan",
            createdAt = "2026-01-15T09:00:00"
        )

        assertEquals("uuid-123", contact.id)
        assertEquals("John Doe", contact.name)
        assertEquals("+1234567890", contact.phone)
        assertEquals("Acme Corp", contact.business)
        assertEquals("Mumbai", contact.city)
        assertEquals("Tech", contact.industry)
        assertEquals(DealStage.Qualified, contact.dealStage)
        assertEquals("2026-02-25T10:30:00", contact.lastCalled)
        assertEquals(3, contact.callCount)
        assertEquals("Discussed pricing", contact.lastCallSummary)
        assertEquals("https://example.com/rec.m4a", contact.recordingLink)
        assertEquals("2026-03-01", contact.nextFollowUp)
        assertEquals("Interested in premium plan", contact.notes)
        assertEquals("2026-01-15T09:00:00", contact.createdAt)
    }

    @Test
    fun `create contact with defaults`() {
        val contact = Contact(
            id = "uuid-456",
            name = "Jane Smith",
            phone = "+0987654321"
        )

        assertEquals("uuid-456", contact.id)
        assertEquals("Jane Smith", contact.name)
        assertEquals("+0987654321", contact.phone)
        assertNull(contact.business)
        assertNull(contact.city)
        assertNull(contact.industry)
        assertEquals(DealStage.New, contact.dealStage)
        assertNull(contact.lastCalled)
        assertEquals(0, contact.callCount)
        assertNull(contact.lastCallSummary)
        assertNull(contact.recordingLink)
        assertNull(contact.nextFollowUp)
        assertNull(contact.notes)
        assertNull(contact.createdAt)
    }

    @Test
    fun `contacts with same data are equal`() {
        val a = Contact(id = "1", name = "A", phone = "123")
        val b = Contact(id = "1", name = "A", phone = "123")
        assertEquals(a, b)
    }

    @Test
    fun `contact copy updates fields`() {
        val original = Contact(id = "1", name = "A", phone = "123")
        val updated = original.copy(dealStage = DealStage.Won, callCount = 5)
        assertEquals(DealStage.Won, updated.dealStage)
        assertEquals(5, updated.callCount)
        assertEquals("A", updated.name)
    }
}
