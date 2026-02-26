package com.aicc.coldcall.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AISummaryTest {
    @Test
    fun `create AI summary with all fields`() {
        val summary = AISummary(
            summary = "Client expressed interest in premium plan. Discussed pricing and timeline.",
            recommendedDealStage = DealStage.Proposal,
            nextAction = "Send proposal document by Friday"
        )

        assertEquals(
            "Client expressed interest in premium plan. Discussed pricing and timeline.",
            summary.summary
        )
        assertEquals(DealStage.Proposal, summary.recommendedDealStage)
        assertEquals("Send proposal document by Friday", summary.nextAction)
    }

    @Test
    fun `AI summaries with same data are equal`() {
        val a = AISummary("summary", DealStage.Contacted, "follow up")
        val b = AISummary("summary", DealStage.Contacted, "follow up")
        assertEquals(a, b)
    }

    @Test
    fun `AI summary copy updates fields`() {
        val original = AISummary("summary", DealStage.New, "call back")
        val updated = original.copy(recommendedDealStage = DealStage.Qualified)
        assertEquals(DealStage.Qualified, updated.recommendedDealStage)
        assertEquals("summary", updated.summary)
    }
}
