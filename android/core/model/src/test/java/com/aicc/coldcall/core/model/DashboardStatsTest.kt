package com.aicc.coldcall.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardStatsTest {
    @Test
    fun `create dashboard stats with all fields`() {
        val pipeline = mapOf(
            DealStage.New to 10,
            DealStage.Contacted to 5,
            DealStage.Qualified to 3,
            DealStage.Proposal to 2,
            DealStage.Negotiation to 1,
            DealStage.Won to 4,
            DealStage.Lost to 2,
            DealStage.NotInterested to 6
        )
        val stats = DashboardStats(
            callsToday = 15,
            connectedToday = 8,
            conversionRate = 0.53,
            streak = 7,
            pipeline = pipeline
        )

        assertEquals(15, stats.callsToday)
        assertEquals(8, stats.connectedToday)
        assertEquals(0.53, stats.conversionRate, 0.001)
        assertEquals(7, stats.streak)
        assertEquals(8, stats.pipeline.size)
        assertEquals(10, stats.pipeline[DealStage.New])
        assertEquals(4, stats.pipeline[DealStage.Won])
    }

    @Test
    fun `create dashboard stats with defaults`() {
        val stats = DashboardStats()

        assertEquals(0, stats.callsToday)
        assertEquals(0, stats.connectedToday)
        assertEquals(0.0, stats.conversionRate, 0.001)
        assertEquals(0, stats.streak)
        assertEquals(0, stats.pipeline.size)
    }

    @Test
    fun `dashboard stats with empty pipeline`() {
        val stats = DashboardStats(
            callsToday = 5,
            connectedToday = 2,
            conversionRate = 0.4,
            streak = 1,
            pipeline = emptyMap()
        )

        assertEquals(5, stats.callsToday)
        assertEquals(0, stats.pipeline.size)
    }
}
