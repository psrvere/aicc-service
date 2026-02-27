package com.aicc.coldcall.feature.dashboard

import com.aicc.coldcall.core.model.DashboardStats
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.data.api.DashboardRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val dashboardRepository = mockk<DashboardRepository>()
    private val testDispatcher = StandardTestDispatcher()

    private val sampleStats = DashboardStats(
        callsToday = 12,
        connectedToday = 8,
        conversionRate = 0.15,
        streak = 5,
        pipeline = mapOf(
            DealStage.New to 10,
            DealStage.Contacted to 7,
            DealStage.Qualified to 3,
        ),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads stats from repository on init`() = runTest {
        coEvery { dashboardRepository.getStats() } returns sampleStats

        val vm = DashboardViewModel(dashboardRepository)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(12, state.stats.callsToday)
        assertEquals(8, state.stats.connectedToday)
        assertEquals(5, state.stats.streak)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `conversion rate stored as decimal`() = runTest {
        coEvery { dashboardRepository.getStats() } returns sampleStats

        val vm = DashboardViewModel(dashboardRepository)
        advanceUntilIdle()

        assertEquals(0.15, vm.uiState.value.stats.conversionRate, 0.001)
    }

    @Test
    fun `pipeline contains all returned deal stages`() = runTest {
        coEvery { dashboardRepository.getStats() } returns sampleStats

        val vm = DashboardViewModel(dashboardRepository)
        advanceUntilIdle()

        val pipeline = vm.uiState.value.stats.pipeline
        assertEquals(3, pipeline.size)
        assertEquals(10, pipeline[DealStage.New])
        assertEquals(7, pipeline[DealStage.Contacted])
        assertEquals(3, pipeline[DealStage.Qualified])
    }

    @Test
    fun `empty pipeline when no data`() = runTest {
        coEvery { dashboardRepository.getStats() } returns DashboardStats()

        val vm = DashboardViewModel(dashboardRepository)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.stats.pipeline.isEmpty())
    }

    @Test
    fun `refresh reloads from repository`() = runTest {
        coEvery { dashboardRepository.getStats() } returns sampleStats

        val vm = DashboardViewModel(dashboardRepository)
        advanceUntilIdle()

        val updatedStats = sampleStats.copy(callsToday = 20, connectedToday = 15)
        coEvery { dashboardRepository.getStats() } returns updatedStats

        vm.refresh()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(20, state.stats.callsToday)
        assertEquals(15, state.stats.connectedToday)
    }

    @Test
    fun `error state when API fails`() = runTest {
        coEvery { dashboardRepository.getStats() } throws RuntimeException("Network error")

        val vm = DashboardViewModel(dashboardRepository)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("Network error", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loading state during fetch`() = runTest {
        coEvery { dashboardRepository.getStats() } returns sampleStats

        val vm = DashboardViewModel(dashboardRepository)

        assertTrue(vm.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
    }
}
