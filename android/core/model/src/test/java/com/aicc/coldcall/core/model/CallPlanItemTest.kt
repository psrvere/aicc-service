package com.aicc.coldcall.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CallPlanItemTest {
    @Test
    fun `create call plan item for new contact`() {
        val item = CallPlanItem(
            id = "item-1",
            name = "New Lead",
            phone = "+1111111111",
            business = "StartupCo",
            dealStage = DealStage.New,
            nextFollowUp = null,
            callCount = 0,
            lastCallSummary = null,
            reason = "New contact"
        )

        assertEquals("item-1", item.id)
        assertEquals("New Lead", item.name)
        assertEquals("+1111111111", item.phone)
        assertEquals("StartupCo", item.business)
        assertEquals(DealStage.New, item.dealStage)
        assertNull(item.nextFollowUp)
        assertEquals(0, item.callCount)
        assertNull(item.lastCallSummary)
        assertEquals("New contact", item.reason)
    }

    @Test
    fun `create call plan item for follow-up`() {
        val item = CallPlanItem(
            id = "item-2",
            name = "Returning Client",
            phone = "+2222222222",
            business = null,
            dealStage = DealStage.Contacted,
            nextFollowUp = "2026-02-26",
            callCount = 2,
            lastCallSummary = "Left voicemail",
            reason = "Follow-up due"
        )

        assertEquals("Follow-up due", item.reason)
        assertEquals("2026-02-26", item.nextFollowUp)
        assertEquals(2, item.callCount)
        assertEquals("Left voicemail", item.lastCallSummary)
    }

    @Test
    fun `call plan item with null optional fields`() {
        val item = CallPlanItem(
            id = "item-3",
            name = "Someone",
            phone = "+3333333333",
            dealStage = DealStage.New,
            callCount = 0,
            reason = "New contact"
        )

        assertNull(item.business)
        assertNull(item.nextFollowUp)
        assertNull(item.lastCallSummary)
    }
}
