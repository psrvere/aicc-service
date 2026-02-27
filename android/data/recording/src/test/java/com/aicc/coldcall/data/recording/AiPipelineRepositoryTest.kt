package com.aicc.coldcall.data.recording

import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.network.AiccApiService
import com.aicc.coldcall.core.network.dto.AISummaryDto
import com.aicc.coldcall.core.network.dto.SummarizeRequestDto
import com.aicc.coldcall.core.network.dto.TranscribeRequestDto
import com.aicc.coldcall.core.network.dto.TranscriptionResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AiPipelineRepositoryTest {

    private val api = mockk<AiccApiService>()
    private lateinit var repository: AiPipelineRepository

    @Before
    fun setUp() {
        repository = AiPipelineRepository(api)
    }

    @Test
    fun `transcribe calls API with recording URL and returns text`() = runTest {
        val requestSlot = slot<TranscribeRequestDto>()
        coEvery { api.transcribe(capture(requestSlot)) } returns TranscriptionResponseDto(
            text = "Hello, this is a sales call..."
        )

        val result = repository.transcribe("https://cdn.example.com/r1.m4a")

        assertEquals("Hello, this is a sales call...", result)
        assertEquals("https://cdn.example.com/r1.m4a", requestSlot.captured.recordingUrl)
    }

    @Test
    fun `summarize calls API with contact ID and transcript, returns AISummary`() = runTest {
        val requestSlot = slot<SummarizeRequestDto>()
        coEvery { api.summarize(capture(requestSlot)) } returns AISummaryDto(
            summary = "Discussed pricing options",
            recommendedDealStage = "Qualified",
            nextAction = "Send proposal by Friday",
        )

        val result = repository.summarize("c1", "Hello, this is a sales call...")

        assertEquals("Discussed pricing options", result.summary)
        assertEquals(DealStage.Qualified, result.recommendedDealStage)
        assertEquals("Send proposal by Friday", result.nextAction)
        assertEquals("c1", requestSlot.captured.contactId)
        assertEquals("Hello, this is a sales call...", requestSlot.captured.transcript)
    }

    @Test
    fun `summarize handles unknown deal stage with default`() = runTest {
        coEvery { api.summarize(any()) } returns AISummaryDto(
            summary = "Summary",
            recommendedDealStage = "UnknownStage",
            nextAction = "Follow up",
        )

        val result = repository.summarize("c1", "transcript")

        assertEquals(DealStage.New, result.recommendedDealStage)
    }
}
