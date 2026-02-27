package com.aicc.coldcall.data.recording

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RecordingUploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val uploader: RecordingUploader,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return if (uploader.uploadPending()) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
