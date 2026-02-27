package com.aicc.coldcall.data.api

import com.aicc.coldcall.core.database.CallLogDao
import com.aicc.coldcall.core.database.CallLogEntity
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.model.Disposition
import com.aicc.coldcall.core.network.AiccApiService
import com.aicc.coldcall.core.network.dto.CallLogCreateDto
import com.aicc.coldcall.core.network.dto.CallLogDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CallLogRepositoryTest {

    private val api = mockk<AiccApiService>()
    private val callLogDao = mockk<CallLogDao>(relaxUnitFun = true)
    private lateinit var repository: CallLogRepository

    @Before
    fun setUp() {
        repository = CallLogRepository(api, callLogDao)
    }

    @Test
    fun `logCall posts to API and caches locally`() = runTest {
        val createDto = CallLogCreateDto(
            contactId = "1",
            durationSeconds = 120,
            disposition = "Connected",
            summary = "Good call",
            dealStage = "Qualified",
        )
        val responseDto = CallLogDto(
            id = "log-1",
            contactId = "1",
            timestamp = "2025-01-15T10:00:00Z",
            durationSeconds = 120,
            disposition = "Connected",
            summary = "Good call",
            dealStage = "Qualified",
        )
        coEvery { api.logCall(createDto) } returns responseDto

        val callLog = repository.logCall(createDto)
        assertEquals("log-1", callLog.id)
        assertEquals("1", callLog.contactId)
        assertEquals(120, callLog.durationSeconds)
        assertEquals(Disposition.Connected, callLog.disposition)
        assertEquals(DealStage.Qualified, callLog.dealStage)

        coVerify { callLogDao.insert(any()) }
    }

    @Test
    fun `logCall maps all fields correctly`() = runTest {
        val createDto = CallLogCreateDto(
            contactId = "2",
            durationSeconds = 60,
            disposition = "NoAnswer",
        )
        val responseDto = CallLogDto(
            id = "log-2",
            contactId = "2",
            timestamp = "2025-01-15T11:00:00Z",
            durationSeconds = 60,
            disposition = "NoAnswer",
            dealStage = "New",
        )
        coEvery { api.logCall(createDto) } returns responseDto

        val callLog = repository.logCall(createDto)
        assertEquals(Disposition.NoAnswer, callLog.disposition)
        assertEquals(DealStage.New, callLog.dealStage)
    }

    @Test
    fun `getByContactId emits mapped call logs from DAO`() = runTest {
        val entities = listOf(
            CallLogEntity(
                id = "log-1",
                contactId = "c1",
                timestamp = "2025-01-15T10:00:00Z",
                durationSeconds = 120,
                disposition = Disposition.Connected,
                summary = "Good call",
                dealStage = DealStage.Qualified,
                recordingUrl = "https://example.com/rec1",
                transcript = "Hello world",
            ),
            CallLogEntity(
                id = "log-2",
                contactId = "c1",
                timestamp = "2025-01-15T11:00:00Z",
                durationSeconds = 60,
                disposition = Disposition.NoAnswer,
                summary = null,
                dealStage = DealStage.New,
                recordingUrl = null,
                transcript = null,
            ),
        )
        every { callLogDao.getByContactId("c1") } returns flowOf(entities)

        val result = repository.getByContactId("c1").first()

        assertEquals(2, result.size)

        val first = result[0]
        assertEquals("log-1", first.id)
        assertEquals("c1", first.contactId)
        assertEquals("2025-01-15T10:00:00Z", first.timestamp)
        assertEquals(120, first.durationSeconds)
        assertEquals(Disposition.Connected, first.disposition)
        assertEquals("Good call", first.summary)
        assertEquals(DealStage.Qualified, first.dealStage)
        assertEquals("https://example.com/rec1", first.recordingUrl)
        assertEquals("Hello world", first.transcript)

        val second = result[1]
        assertEquals("log-2", second.id)
        assertEquals("c1", second.contactId)
        assertEquals(60, second.durationSeconds)
        assertEquals(Disposition.NoAnswer, second.disposition)
        assertEquals(null, second.summary)
        assertEquals(DealStage.New, second.dealStage)
        assertEquals(null, second.recordingUrl)
        assertEquals(null, second.transcript)

        verify(exactly = 0) { api.hashCode() } // no API interactions
    }
}
