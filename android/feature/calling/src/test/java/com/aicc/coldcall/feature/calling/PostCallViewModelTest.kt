package com.aicc.coldcall.feature.calling

import com.aicc.coldcall.core.model.CallLog
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.model.Disposition
import com.aicc.coldcall.core.network.dto.CallLogCreateDto
import com.aicc.coldcall.data.api.CallLogRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PostCallViewModelTest {

    private val callLogRepository = mockk<CallLogRepository>()
    private val testDispatcher = StandardTestDispatcher()

    private val today = LocalDate.of(2026, 2, 27)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createVm() = PostCallViewModel(
        callLogRepository = callLogRepository,
        contactId = "c1",
        contactName = "Alice",
        clock = { today },
    )

    @Test
    fun `initial state has no disposition selected`() = runTest {
        val vm = createVm()
        advanceUntilIdle()

        assertNull(vm.uiState.value.disposition)
    }

    @Test
    fun `selecting disposition updates state`() = runTest {
        val vm = createVm()
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        assertEquals(Disposition.Connected, vm.uiState.value.disposition)
    }

    @Test
    fun `Callback disposition sets follow-up to tomorrow`() = runTest {
        val vm = createVm()
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Callback)
        assertEquals(today.plusDays(1).toString(), vm.uiState.value.followUpDate)
    }

    @Test
    fun `NoAnswer disposition sets follow-up to plus 3 days`() = runTest {
        val vm = createVm()
        advanceUntilIdle()

        vm.selectDisposition(Disposition.NoAnswer)
        assertEquals(today.plusDays(3).toString(), vm.uiState.value.followUpDate)
    }

    @Test
    fun `Voicemail disposition sets follow-up to plus 7 days`() = runTest {
        val vm = createVm()
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Voicemail)
        assertEquals(today.plusDays(7).toString(), vm.uiState.value.followUpDate)
    }

    @Test
    fun `Connected disposition has no auto follow-up`() = runTest {
        val vm = createVm()
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        assertNull(vm.uiState.value.followUpDate)
    }

    @Test
    fun `NotInterested disposition has no auto follow-up`() = runTest {
        val vm = createVm()
        advanceUntilIdle()

        vm.selectDisposition(Disposition.NotInterested)
        assertNull(vm.uiState.value.followUpDate)
    }

    @Test
    fun `WrongNumber disposition has no auto follow-up`() = runTest {
        val vm = createVm()
        advanceUntilIdle()

        vm.selectDisposition(Disposition.WrongNumber)
        assertNull(vm.uiState.value.followUpDate)
    }

    @Test
    fun `saveCall creates CallLogCreateDto and calls repository`() = runTest {
        val dtoSlot = slot<CallLogCreateDto>()
        coEvery { callLogRepository.logCall(capture(dtoSlot)) } returns CallLog(
            id = "log1",
            contactId = "c1",
            timestamp = "2026-02-27T10:00:00",
            durationSeconds = 0,
            disposition = Disposition.Connected,
            dealStage = DealStage.Contacted,
        )

        val vm = createVm()
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        vm.updateSummary("Great call")
        vm.saveCall()
        advanceUntilIdle()

        coVerify { callLogRepository.logCall(any()) }
        val dto = dtoSlot.captured
        assertEquals("c1", dto.contactId)
        assertEquals(0, dto.durationSeconds)
        assertEquals("Connected", dto.disposition)
        assertEquals("Great call", dto.summary)
        assertNull(dto.nextFollowUp)
    }

    @Test
    fun `saveCall sets isSaved on success`() = runTest {
        coEvery { callLogRepository.logCall(any()) } returns CallLog(
            id = "log1",
            contactId = "c1",
            timestamp = "2026-02-27T10:00:00",
            durationSeconds = 0,
            disposition = Disposition.Connected,
            dealStage = DealStage.Contacted,
        )

        val vm = createVm()
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        vm.saveCall()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSaving)
        assertTrue(vm.uiState.value.isSaved)
    }

    @Test
    fun `error state on save failure`() = runTest {
        coEvery { callLogRepository.logCall(any()) } throws RuntimeException("Server error")

        val vm = createVm()
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        vm.saveCall()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("Server error", state.error)
        assertFalse(state.isSaving)
        assertFalse(state.isSaved)
    }

    @Test
    fun `saveCall with Callback sends follow-up date`() = runTest {
        val dtoSlot = slot<CallLogCreateDto>()
        coEvery { callLogRepository.logCall(capture(dtoSlot)) } returns CallLog(
            id = "log1",
            contactId = "c1",
            timestamp = "2026-02-27T10:00:00",
            durationSeconds = 0,
            disposition = Disposition.Callback,
            dealStage = DealStage.Contacted,
        )

        val vm = createVm()
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Callback)
        vm.saveCall()
        advanceUntilIdle()

        assertEquals(today.plusDays(1).toString(), dtoSlot.captured.nextFollowUp)
    }
}
