package com.aicc.coldcall.data.recording

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class RecordingFileManagerTest {

    private lateinit var tempDir: File
    private lateinit var manager: RecordingFileManager

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "recording_test_${System.nanoTime()}")
        tempDir.mkdirs()
        manager = RecordingFileManager(tempDir)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `createRecordingFile returns file with correct name pattern`() {
        val file = manager.createRecordingFile("contact123", 1000L)

        assertEquals("call_contact123_1000.m4a", file.name)
    }

    @Test
    fun `createRecordingFile creates recordings subdirectory`() {
        val file = manager.createRecordingFile("c1", 500L)

        val recordingsDir = File(tempDir, "recordings")
        assertTrue(recordingsDir.exists())
        assertTrue(recordingsDir.isDirectory)
        assertEquals(recordingsDir, file.parentFile)
    }

    @Test
    fun `createRecordingFile creates the file on disk`() {
        val file = manager.createRecordingFile("c1", 100L)

        assertTrue(file.exists())
    }

    @Test(expected = IllegalStateException::class)
    fun `createRecordingFile throws when file already exists`() {
        manager.createRecordingFile("c1", 100L)
        manager.createRecordingFile("c1", 100L) // same contactId + timestamp
    }

    @Test
    fun `deleteFilesOlderThan removes old files and keeps recent`() {
        val now = System.currentTimeMillis()
        val oldFile = manager.createRecordingFile("c1", 100L)
        oldFile.setLastModified(now - 10_000)
        val recentFile = manager.createRecordingFile("c2", 200L)
        recentFile.setLastModified(now)

        val deleted = manager.deleteFilesOlderThan(now - 5_000)

        assertEquals(1, deleted)
        assertTrue(recentFile.exists())
    }

    @Test
    fun `deleteFilesOlderThan returns zero when no files to delete`() {
        val deleted = manager.deleteFilesOlderThan(System.currentTimeMillis())

        assertEquals(0, deleted)
    }
}
