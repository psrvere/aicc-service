package com.aicc.coldcall.data.recording

import com.aicc.coldcall.core.database.RecordingEntity
import com.aicc.coldcall.core.network.AiccApiService
import com.aicc.coldcall.core.network.dto.RecordingUploadResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class RecordingUploadWorkerTest {

    private val api = mockk<AiccApiService>()
    private val recordingRepository = mockk<RecordingRepository>(relaxUnitFun = true)
    private lateinit var uploader: RecordingUploader
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "upload_test_${System.nanoTime()}")
        tempDir.mkdirs()
        uploader = RecordingUploader(api, recordingRepository)
    }

    @Test
    fun `uploadPending uploads files and marks uploaded`() = runTest {
        val file = File(tempDir, "call_c1_100.m4a").apply { createNewFile() }
        val entity = RecordingEntity(
            id = 1L,
            contactId = "c1",
            filePath = file.absolutePath,
            createdAt = 100L,
        )
        coEvery { recordingRepository.getPendingUploads() } returns listOf(entity)
        coEvery { api.uploadRecording(any()) } returns RecordingUploadResponseDto("https://cdn.example.com/r1.m4a")

        val success = uploader.uploadPending()

        assertTrue(success)
        coVerify { recordingRepository.markUploaded(1L, "https://cdn.example.com/r1.m4a") }
    }

    @Test
    fun `uploadPending returns false on API failure`() = runTest {
        val file = File(tempDir, "call_c1_100.m4a").apply { createNewFile() }
        val entity = RecordingEntity(
            id = 1L,
            contactId = "c1",
            filePath = file.absolutePath,
            createdAt = 100L,
        )
        coEvery { recordingRepository.getPendingUploads() } returns listOf(entity)
        coEvery { api.uploadRecording(any()) } throws RuntimeException("Upload failed")

        val success = uploader.uploadPending()

        assertFalse(success)
        coVerify(exactly = 0) { recordingRepository.markUploaded(any(), any()) }
    }

    @Test
    fun `uploadSingle uploads one file and returns remote URL`() = runTest {
        val file = File(tempDir, "call_c1_100.m4a").apply { createNewFile() }
        coEvery { api.uploadRecording(any()) } returns RecordingUploadResponseDto("https://cdn.example.com/r1.m4a")

        val url = uploader.uploadSingle(1L, file.absolutePath)

        assertEquals("https://cdn.example.com/r1.m4a", url)
        coVerify { recordingRepository.markUploaded(1L, "https://cdn.example.com/r1.m4a") }
    }

    @Test
    fun `uploadFile uploads and returns URL without marking DB`() = runTest {
        val file = File(tempDir, "call_c1_100.m4a").apply { createNewFile() }
        coEvery { api.uploadRecording(any()) } returns RecordingUploadResponseDto("https://cdn.example.com/r1.m4a")

        val url = uploader.uploadFile(file.absolutePath)

        assertEquals("https://cdn.example.com/r1.m4a", url)
        coVerify(exactly = 0) { recordingRepository.markUploaded(any(), any()) }
    }

    @Test(expected = IllegalStateException::class)
    fun `uploadFile throws when file does not exist`() = runTest {
        uploader.uploadFile("/nonexistent/path/recording.m4a")
    }

    @Test
    fun `uploadPending returns true when no pending uploads`() = runTest {
        coEvery { recordingRepository.getPendingUploads() } returns emptyList()

        val success = uploader.uploadPending()

        assertTrue(success)
    }
}
