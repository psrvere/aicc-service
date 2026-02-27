package com.aicc.coldcall.data.recording

import android.util.Log
import com.aicc.coldcall.core.network.AiccApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingUploader @Inject constructor(
    private val api: AiccApiService,
    private val recordingRepository: RecordingRepository,
) {

    suspend fun uploadPending(): Boolean {
        val pending = recordingRepository.getPendingUploads()
        if (pending.isEmpty()) return true
        return try {
            pending.forEach { entity ->
                uploadSingle(entity.id, entity.filePath)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload pending recordings", e)
            false
        }
    }

    suspend fun uploadFile(filePath: String): String {
        val file = File(filePath)
        if (!file.isFile) {
            throw IllegalStateException("Recording file not found: $filePath")
        }
        val requestBody = file.asRequestBody("audio/mp4".toMediaType())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val response = api.uploadRecording(part)
        return response.url
    }

    suspend fun uploadSingle(recordingId: Long, filePath: String): String {
        val url = uploadFile(filePath)
        recordingRepository.markUploaded(recordingId, url)
        return url
    }

    companion object {
        private const val TAG = "RecordingUploader"
    }
}
