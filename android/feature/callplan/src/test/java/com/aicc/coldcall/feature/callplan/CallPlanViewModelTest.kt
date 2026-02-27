package com.aicc.coldcall.feature.callplan

import com.aicc.coldcall.core.model.CallPlanItem
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.data.api.CallPlanRepository
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
class CallPlanViewModelTest {

    private val callPlanRepository = mockk<CallPlanRepository>()
    private val testDispatcher = StandardTestDispatcher()

    private val sampleItems = listOf(
        CallPlanItem(
            id = "1",
            name = "Alice",
            phone = "+1234567890",
            business = "Acme Corp",
            dealStage = DealStage.Contacted,
            callCount = 2,
            lastCallSummary = "Discussed pricing",
            reason = "Follow-up: Callback scheduled",
        ),
        CallPlanItem(
            id = "2",
            name = "Bob",
            phone = "+0987654321",
            business = "Widget Inc",
            dealStage = DealStage.New,
            callCount = 0,
            reason = "New lead from import",
        ),
        CallPlanItem(
            id = "3",
            name = "Carol",
            phone = "+1112223333",
            business = "Tech LLC",
            dealStage = DealStage.Qualified,
            callCount = 3,
            reason = "Follow-up: No answer yesterday",
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
    fun `loads today plan from repository on init`() = runTest {
        coEvery { callPlanRepository.getTodayPlan() } returns sampleItems

        val vm = CallPlanViewModel(callPlanRepository)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(3, state.planItems.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `separates follow-ups from new contacts`() = runTest {
        coEvery { callPlanRepository.getTodayPlan() } returns sampleItems

        val vm = CallPlanViewModel(callPlanRepository)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.followUpCount)
        assertEquals(1, state.newContactCount)
    }

    @Test
    fun `markAsCompleted adds contact to completed set`() = runTest {
        coEvery { callPlanRepository.getTodayPlan() } returns sampleItems

        val vm = CallPlanViewModel(callPlanRepository)
        advanceUntilIdle()

        vm.markAsCompleted("1")
        val state = vm.uiState.value
        assertTrue(state.completedIds.contains("1"))
        assertFalse(state.completedIds.contains("2"))
    }

    @Test
    fun `refresh reloads from repository`() = runTest {
        coEvery { callPlanRepository.getTodayPlan() } returns sampleItems

        val vm = CallPlanViewModel(callPlanRepository)
        advanceUntilIdle()

        val updatedItems = sampleItems.take(1)
        coEvery { callPlanRepository.getTodayPlan() } returns updatedItems

        vm.refresh()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.planItems.size)
    }

    @Test
    fun `refresh preserves completedIds for items still in plan`() = runTest {
        coEvery { callPlanRepository.getTodayPlan() } returns sampleItems

        val vm = CallPlanViewModel(callPlanRepository)
        advanceUntilIdle()

        vm.markAsCompleted("1")
        vm.markAsCompleted("2")

        vm.refresh()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.completedIds.contains("1"))
        assertTrue(state.completedIds.contains("2"))
    }

    @Test
    fun `refresh removes completedIds for items no longer in plan`() = runTest {
        coEvery { callPlanRepository.getTodayPlan() } returns sampleItems

        val vm = CallPlanViewModel(callPlanRepository)
        advanceUntilIdle()

        vm.markAsCompleted("1")
        vm.markAsCompleted("3")

        // Refresh returns only item "1"
        coEvery { callPlanRepository.getTodayPlan() } returns sampleItems.take(1)

        vm.refresh()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.completedIds.contains("1"))
        assertFalse(state.completedIds.contains("3"))
    }

    @Test
    fun `error state when API fails`() = runTest {
        coEvery { callPlanRepository.getTodayPlan() } throws RuntimeException("Network error")

        val vm = CallPlanViewModel(callPlanRepository)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("Network error", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loading state during fetch`() = runTest {
        coEvery { callPlanRepository.getTodayPlan() } returns sampleItems

        val vm = CallPlanViewModel(callPlanRepository)

        assertTrue(vm.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
    }
}
