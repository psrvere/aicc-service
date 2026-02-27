package com.aicc.coldcall.data.recording

import java.io.File
import java.io.IOException

class RecordingFileManager(private val filesDir: File) {

    private val recordingsDir = File(filesDir, "recordings")

    fun createRecordingFile(contactId: String, timestamp: Long): File {
        if (!recordingsDir.exists() && !recordingsDir.mkdirs()) {
            throw IOException("Failed to create recordings directory: ${recordingsDir.absolutePath}")
        }
        if (!recordingsDir.isDirectory) {
            throw IOException("Recordings path is not a directory: ${recordingsDir.absolutePath}")
        }
        val sanitizedId = contactId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "call_${sanitizedId}_$timestamp.m4a"
        val file = File(recordingsDir, fileName)
        if (!file.createNewFile()) {
            throw IllegalStateException(
                "Recording file already exists: $fileName (contactId=$contactId, timestamp=$timestamp)"
            )
        }
        return file
    }

    fun deleteFilesOlderThan(thresholdMs: Long): Int {
        if (!recordingsDir.isDirectory) return 0
        val oldFiles = recordingsDir.listFiles()?.filter { it.lastModified() < thresholdMs } ?: return 0
        return oldFiles.count { it.delete() }
    }
}
