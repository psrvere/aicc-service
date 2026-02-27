package com.aicc.coldcall.data.recording

import com.aicc.coldcall.core.database.RecordingDao
import com.aicc.coldcall.core.database.RecordingEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File

class RecordingRepositoryTest {

    private val recordingDao = mockk<RecordingDao>(relaxUnitFun = true)
    private val fileManager = mockk<RecordingFileManager>()
    private val clock: () -> Long = { 1000L }
    private lateinit var repository: RecordingRepository

    @Before
    fun setUp() {
        repository = RecordingRepository(recordingDao, fileManager, clock)
    }

    @Test
    fun `startRecording creates file, inserts entity, returns path`() = runTest {
        val file = mockk<File>()
        every { file.absolutePath } returns "/data/recordings/call_c1_1000.m4a"
        every { fileManager.createRecordingFile("c1", 1000L) } returns file
        coEvery { recordingDao.insert(any()) } returns 42L

        val path = repository.startRecording("c1")

        assertEquals("/data/recordings/call_c1_1000.m4a", path)
        val entitySlot = slot<RecordingEntity>()
        coVerify { recordingDao.insert(capture(entitySlot)) }
        val entity = entitySlot.captured
        assertEquals("c1", entity.contactId)
        assertEquals("/data/recordings/call_c1_1000.m4a", entity.filePath)
        assertEquals(1000L, entity.createdAt)
    }

    @Test
    fun `stopRecording returns current file path`() = runTest {
        val file = mockk<File>()
        every { file.absolutePath } returns "/data/recordings/call_c1_1000.m4a"
        every { fileManager.createRecordingFile("c1", 1000L) } returns file
        coEvery { recordingDao.insert(any()) } returns 42L
        repository.startRecording("c1")

        val result = repository.stopRecording()

        assertEquals("/data/recordings/call_c1_1000.m4a", result)
    }

    @Test
    fun `stopRecording returns null when no recording active`() = runTest {
        val result = repository.stopRecording()

        assertNull(result)
    }

    @Test
    fun `getPendingUploads delegates to DAO`() = runTest {
        val entities = listOf(
            RecordingEntity(id = 1, contactId = "c1", filePath = "/path", createdAt = 100L),
        )
        coEvery { recordingDao.getPendingUploads() } returns entities

        val result = repository.getPendingUploads()

        assertEquals(entities, result)
        coVerify { recordingDao.getPendingUploads() }
    }

    @Test
    fun `markUploaded delegates to DAO`() = runTest {
        repository.markUploaded(1L, "https://example.com/recording.m4a")

        coVerify { recordingDao.markUploaded(1L, "https://example.com/recording.m4a") }
    }

    @Test
    fun `getCurrentRecordingId returns id after startRecording`() = runTest {
        val file = mockk<File>()
        every { file.absolutePath } returns "/path/file.m4a"
        every { fileManager.createRecordingFile("c1", 1000L) } returns file
        coEvery { recordingDao.insert(any()) } returns 99L

        repository.startRecording("c1")

        assertEquals(99L, repository.getCurrentRecordingId())
    }
}
