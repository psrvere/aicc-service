package com.aicc.coldcall.data.recording

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class CallRecordingService : Service() {

    @Inject lateinit var recordingRepository: RecordingRepository

    private var mediaRecorder: MediaRecorder? = null
    private val recorderMutex = Mutex()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val contactId = intent.getStringExtra(EXTRA_CONTACT_ID) ?: run {
                    stopSelf()
                    return START_NOT_STICKY
                }
                startRecording(contactId)
            }
            ACTION_STOP -> stopRecording()
            else -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startRecording(contactId: String) {
        createNotificationChannel()
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        serviceScope.launch {
            recorderMutex.withLock {
                try {
                    val filePath = recordingRepository.startRecording(contactId)
                    val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        MediaRecorder(this@CallRecordingService)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaRecorder()
                    }
                    recorder.apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        setAudioEncodingBitRate(128_000)
                        setAudioSamplingRate(44_100)
                        setOutputFile(filePath)
                        prepare()
                        start()
                    }
                    mediaRecorder = recorder
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start recording", e)
                    stopSelf()
                }
            }
        }
    }

    private fun stopRecording() {
        serviceScope.launch {
            recorderMutex.withLock {
                mediaRecorder?.let { recorder ->
                    try {
                        recorder.stop()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error stopping MediaRecorder", e)
                    }
                    try {
                        recorder.release()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error releasing MediaRecorder", e)
                    }
                }
                mediaRecorder = null
            }

            withContext(NonCancellable) {
                recordingRepository.stopRecording()
                WorkManager.getInstance(applicationContext)
                    .enqueue(OneTimeWorkRequestBuilder<RecordingUploadWorker>().build())
            }
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Call Recording",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows when a call is being recorded"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Recording call")
            .setContentText("Call recording in progress")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "CallRecordingService"
        private const val CHANNEL_ID = "call_recording"
        private const val NOTIFICATION_ID = 101
        private const val ACTION_START = "com.aicc.coldcall.START_RECORDING"
        private const val ACTION_STOP = "com.aicc.coldcall.STOP_RECORDING"
        private const val EXTRA_CONTACT_ID = "contact_id"

        fun startIntent(context: Context, contactId: String): Intent =
            Intent(context, CallRecordingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_CONTACT_ID, contactId)
            }

        fun stopIntent(context: Context): Intent =
            Intent(context, CallRecordingService::class.java).apply {
                action = ACTION_STOP
            }
    }
}
