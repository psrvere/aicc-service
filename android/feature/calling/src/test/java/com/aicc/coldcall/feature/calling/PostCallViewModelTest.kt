package com.aicc.coldcall.feature.calling

import com.aicc.coldcall.core.model.AISummary
import com.aicc.coldcall.core.model.CallLog
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.model.Disposition
import com.aicc.coldcall.core.network.dto.CallLogCreateDto
import com.aicc.coldcall.data.api.CallLogRepository
import com.aicc.coldcall.data.recording.AiPipelineRepository
import com.aicc.coldcall.data.recording.RecordingUploader
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
    private val aiPipelineRepository = mockk<AiPipelineRepository>()
    private val recordingUploader = mockk<RecordingUploader>()
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

    private fun createVm(recordingFilePath: String? = null) = PostCallViewModel(
        callLogRepository = callLogRepository,
        aiPipelineRepository = aiPipelineRepository,
        recordingUploader = recordingUploader,
        contactId = "c1",
        contactName = "Alice",
        clock = { today },
        recordingFilePath = recordingFilePath,
    )

    private fun stubCallLog() {
        coEvery { callLogRepository.logCall(any()) } returns CallLog(
            id = "log1",
            contactId = "c1",
            timestamp = "2026-02-27T10:00:00",
            durationSeconds = 0,
            disposition = Disposition.Connected,
            dealStage = DealStage.Contacted,
        )
    }

    // --- Existing tests ---

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
        stubCallLog()

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

    // --- AI Pipeline tests ---

    @Test
    fun `Connected with recording triggers AI pipeline after save`() = runTest {
        stubCallLog()
        coEvery { recordingUploader.uploadSingle(any(), any()) } returns "https://cdn.example.com/r1.m4a"
        coEvery { aiPipelineRepository.transcribe(any()) } returns "Hello, sales call transcript"
        coEvery { aiPipelineRepository.summarize(any(), any()) } returns AISummary(
            summary = "Discussed pricing",
            recommendedDealStage = DealStage.Qualified,
            nextAction = "Send proposal",
        )

        val vm = createVm(recordingFilePath = "/path/to/recording.m4a")
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        vm.saveCall()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(AiPipelineStatus.COMPLETED, state.aiPipelineStatus)
        assertEquals("Discussed pricing", state.aiSummary?.summary)
        assertEquals(DealStage.Qualified, state.aiSummary?.recommendedDealStage)
        assertEquals("Send proposal", state.aiSummary?.nextAction)
        assertEquals("https://cdn.example.com/r1.m4a", state.recordingUrl)
        assertEquals("Hello, sales call transcript", state.transcript)
    }

    @Test
    fun `AI pipeline progresses through status transitions`() = runTest {
        stubCallLog()
        val statuses = mutableListOf<AiPipelineStatus?>()

        coEvery { recordingUploader.uploadSingle(any(), any()) } coAnswers {
            statuses.add(AiPipelineStatus.UPLOADING)
            "https://cdn.example.com/r1.m4a"
        }
        coEvery { aiPipelineRepository.transcribe(any()) } coAnswers {
            statuses.add(AiPipelineStatus.TRANSCRIBING)
            "transcript"
        }
        coEvery { aiPipelineRepository.summarize(any(), any()) } coAnswers {
            statuses.add(AiPipelineStatus.SUMMARIZING)
            AISummary("summary", DealStage.New, "action")
        }

        val vm = createVm(recordingFilePath = "/path/recording.m4a")
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        vm.saveCall()
        advanceUntilIdle()

        // Each step was reached before the next was called
        assertEquals(
            listOf(AiPipelineStatus.UPLOADING, AiPipelineStatus.TRANSCRIBING, AiPipelineStatus.SUMMARIZING),
            statuses,
        )
        assertEquals(AiPipelineStatus.COMPLETED, vm.uiState.value.aiPipelineStatus)
    }

    @Test
    fun `user edits AI summary updates state`() = runTest {
        stubCallLog()
        coEvery { recordingUploader.uploadSingle(any(), any()) } returns "https://cdn.example.com/r1.m4a"
        coEvery { aiPipelineRepository.transcribe(any()) } returns "transcript"
        coEvery { aiPipelineRepository.summarize(any(), any()) } returns AISummary(
            summary = "Original summary",
            recommendedDealStage = DealStage.Qualified,
            nextAction = "Follow up",
        )

        val vm = createVm(recordingFilePath = "/path/recording.m4a")
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        vm.saveCall()
        advanceUntilIdle()

        vm.updateAiSummary("Edited summary")

        assertEquals("Edited summary", vm.uiState.value.aiSummary?.summary)
        // Other fields preserved
        assertEquals(DealStage.Qualified, vm.uiState.value.aiSummary?.recommendedDealStage)
    }

    @Test
    fun `regenerateSummary re-calls summarize with existing transcript`() = runTest {
        stubCallLog()
        coEvery { recordingUploader.uploadSingle(any(), any()) } returns "https://cdn.example.com/r1.m4a"
        coEvery { aiPipelineRepository.transcribe(any()) } returns "transcript text"
        coEvery { aiPipelineRepository.summarize("c1", "transcript text") } returns AISummary(
            summary = "First summary",
            recommendedDealStage = DealStage.Qualified,
            nextAction = "Send proposal",
        ) andThen AISummary(
            summary = "Regenerated summary",
            recommendedDealStage = DealStage.Proposal,
            nextAction = "Schedule demo",
        )

        val vm = createVm(recordingFilePath = "/path/recording.m4a")
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        vm.saveCall()
        advanceUntilIdle()

        assertEquals("First summary", vm.uiState.value.aiSummary?.summary)

        vm.regenerateSummary()
        advanceUntilIdle()

        assertEquals("Regenerated summary", vm.uiState.value.aiSummary?.summary)
        assertEquals(DealStage.Proposal, vm.uiState.value.aiSummary?.recommendedDealStage)
        assertEquals(AiPipelineStatus.COMPLETED, vm.uiState.value.aiPipelineStatus)
    }

    @Test
    fun `non-Connected disposition does not trigger AI pipeline`() = runTest {
        coEvery { callLogRepository.logCall(any()) } returns CallLog(
            id = "log1",
            contactId = "c1",
            timestamp = "2026-02-27T10:00:00",
            durationSeconds = 0,
            disposition = Disposition.NoAnswer,
            dealStage = DealStage.Contacted,
        )

        val vm = createVm(recordingFilePath = "/path/recording.m4a")
        advanceUntilIdle()

        vm.selectDisposition(Disposition.NoAnswer)
        vm.saveCall()
        advanceUntilIdle()

        assertNull(vm.uiState.value.aiPipelineStatus)
        coVerify(exactly = 0) { recordingUploader.uploadSingle(any(), any()) }
    }

    @Test
    fun `no recording file does not trigger AI pipeline even for Connected`() = runTest {
        stubCallLog()

        val vm = createVm(recordingFilePath = null)
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        vm.saveCall()
        advanceUntilIdle()

        assertNull(vm.uiState.value.aiPipelineStatus)
        coVerify(exactly = 0) { recordingUploader.uploadSingle(any(), any()) }
    }

    @Test
    fun `confirmAiSummary persists AI data and clears pipeline status`() = runTest {
        stubCallLog()
        coEvery { recordingUploader.uploadSingle(any(), any()) } returns "https://cdn.example.com/r1.m4a"
        coEvery { aiPipelineRepository.transcribe(any()) } returns "transcript text"
        coEvery { aiPipelineRepository.summarize(any(), any()) } returns AISummary(
            summary = "AI summary",
            recommendedDealStage = DealStage.Qualified,
            nextAction = "Send proposal",
        )

        val vm = createVm(recordingFilePath = "/path/recording.m4a")
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        vm.saveCall()
        advanceUntilIdle()
        assertEquals(AiPipelineStatus.COMPLETED, vm.uiState.value.aiPipelineStatus)

        val dtoSlot = slot<CallLogCreateDto>()
        coEvery { callLogRepository.logCall(capture(dtoSlot)) } returns CallLog(
            id = "log2",
            contactId = "c1",
            timestamp = "2026-02-27T10:00:00",
            durationSeconds = 0,
            disposition = Disposition.Connected,
            dealStage = DealStage.Qualified,
        )

        vm.confirmAiSummary()
        advanceUntilIdle()

        val dto = dtoSlot.captured
        assertEquals("AI summary", dto.summary)
        assertEquals("Qualified", dto.dealStage)
        assertEquals("https://cdn.example.com/r1.m4a", dto.recordingUrl)
        assertEquals("transcript text", dto.transcript)
        assertNull(vm.uiState.value.aiPipelineStatus)
        assertTrue(vm.uiState.value.isSaved)
    }

    @Test
    fun `confirmAiSummary persists user-edited summary`() = runTest {
        stubCallLog()
        coEvery { recordingUploader.uploadSingle(any(), any()) } returns "https://cdn.example.com/r1.m4a"
        coEvery { aiPipelineRepository.transcribe(any()) } returns "transcript"
        coEvery { aiPipelineRepository.summarize(any(), any()) } returns AISummary(
            summary = "Original",
            recommendedDealStage = DealStage.New,
            nextAction = "Follow up",
        )

        val vm = createVm(recordingFilePath = "/path/recording.m4a")
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        vm.saveCall()
        advanceUntilIdle()

        vm.updateAiSummary("User-edited summary")

        val dtoSlot = slot<CallLogCreateDto>()
        coEvery { callLogRepository.logCall(capture(dtoSlot)) } returns CallLog(
            id = "log2",
            contactId = "c1",
            timestamp = "2026-02-27T10:00:00",
            durationSeconds = 0,
            disposition = Disposition.Connected,
            dealStage = DealStage.New,
        )

        vm.confirmAiSummary()
        advanceUntilIdle()

        assertEquals("User-edited summary", dtoSlot.captured.summary)
    }

    @Test
    fun `AI pipeline error sets ERROR status and message`() = runTest {
        stubCallLog()
        coEvery { recordingUploader.uploadSingle(any(), any()) } throws RuntimeException("Upload failed")

        val vm = createVm(recordingFilePath = "/path/recording.m4a")
        advanceUntilIdle()

        vm.selectDisposition(Disposition.Connected)
        vm.saveCall()
        advanceUntilIdle()

        assertEquals(AiPipelineStatus.ERROR, vm.uiState.value.aiPipelineStatus)
        assertEquals("Upload failed", vm.uiState.value.aiError)
    }
}
