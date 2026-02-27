package com.aicc.coldcall.data.recording

import android.util.Log
import com.aicc.coldcall.core.database.RecordingDao
import com.aicc.coldcall.core.database.RecordingEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingRepository @Inject constructor(
    private val recordingDao: RecordingDao,
    private val fileManager: RecordingFileManager,
    @SystemClock private val clock: () -> Long,
) {

    private val mutex = Mutex()
    private var currentFilePath: String? = null
    private var currentRecordingId: Long? = null

    suspend fun startRecording(contactId: String): String = mutex.withLock {
        val timestamp = clock()
        val file = fileManager.createRecordingFile(contactId, timestamp)
        val entity = RecordingEntity(
            contactId = contactId,
            filePath = file.absolutePath,
            createdAt = timestamp,
        )
        val id = recordingDao.insert(entity)
        currentFilePath = file.absolutePath
        currentRecordingId = id
        file.absolutePath
    }

    suspend fun stopRecording(): String? = mutex.withLock {
        val path = currentFilePath
        currentFilePath = null
        currentRecordingId = null
        path
    }

    suspend fun getPendingUploads(): List<RecordingEntity> =
        recordingDao.getPendingUploads()

    suspend fun markUploaded(id: Long, url: String) =
        recordingDao.markUploaded(id, url)

    suspend fun getCurrentRecordingId(): Long? = mutex.withLock { currentRecordingId }

    suspend fun deleteRecording(id: Long, filePath: String) = mutex.withLock {
        try {
            recordingDao.deleteById(id)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete recording row $id", e)
        }
        try {
            File(filePath).delete()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete recording file $filePath", e)
        }
        if (currentRecordingId == id) {
            currentFilePath = null
            currentRecordingId = null
        }
    }

    companion object {
        private const val TAG = "RecordingRepository"
    }
}
