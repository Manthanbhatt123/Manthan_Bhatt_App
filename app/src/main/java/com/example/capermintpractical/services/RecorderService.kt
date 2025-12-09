package com.example.capermintpractical.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.DocumentsContract
import androidx.core.app.NotificationCompat
import com.example.capermintpractical.R
import com.example.capermintpractical.model.Events
import com.example.capermintpractical.presentation.main.MainActivity
import com.example.capermintpractical.presentation.utility.CANCEL_RECORDING
import com.example.capermintpractical.presentation.utility.Config
import com.example.capermintpractical.presentation.utility.EXTENSION_MP3
import com.example.capermintpractical.presentation.utility.GET_RECORDER_INFO
import com.example.capermintpractical.presentation.utility.RECORDER_RUNNING_NOTIF_ID
import com.example.capermintpractical.presentation.utility.RECORDING_PAUSED
import com.example.capermintpractical.presentation.utility.RECORDING_RUNNING
import com.example.capermintpractical.presentation.utility.RECORDING_STOPPED
import com.example.capermintpractical.presentation.utility.STOP_AMPLITUDE_UPDATE
import com.example.capermintpractical.presentation.utility.TOGGLE_PAUSE
import com.example.capermintpractical.recorder.Recorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.fossify.voicerecorder.recorder.MediaRecorderWrapper
import org.fossify.voicerecorder.recorder.Mp3Recorder
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.Timer
import java.util.TimerTask
import kotlin.coroutines.coroutineContext

class RecorderService : Service() {

    val Context.config: Config get() = Config.newInstance(applicationContext)
    companion object {
        var isRunning = false

        private const val AMPLITUDE_UPDATE_MS = 75L
    }


    private var recordingFile = ""
    private var duration = 0
    private var status = RECORDING_STOPPED
    private var durationTimer = Timer()
    private var amplitudeTimer = Timer()
    private var recorder: Recorder? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent.action) {
            GET_RECORDER_INFO -> broadcastRecorderInfo()
            STOP_AMPLITUDE_UPDATE -> amplitudeTimer.cancel()
            TOGGLE_PAUSE -> togglePause()
            CANCEL_RECORDING -> cancelRecording()
            else -> startRecording()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        isRunning = false
//        updateWidgets(false)
    }

    // mp4 output format with aac encoding should produce good enough m4a files according to https://stackoverflow.com/a/33054794/1967672
    @SuppressLint("DiscouragedApi")
    private fun startRecording() {
        isRunning = true
//        updateWidgets(true)
        if (status == RECORDING_RUNNING) {
            return
        }

        /*val defaultFolder = File(config.saveRecordingsFolder)
        if (!defaultFolder.exists()) {
            defaultFolder.mkdir()
        }*/

//        val recordingFolder = defaultFolder.absolutePath
//        recordingFile = "$recordingFolder/${Config(this).getCurrentFormattedDateTime()}.${config.getExtension()}"

        try {
            recorder?.release()
            recorder = null
            recorder = /*if (recordMp3()) {*/
                Mp3Recorder(this)
           /* } else {
                MediaRecorderWrapper(this)
            }*/

            /*if (isRPlus()) {
                val fileUri = createDocumentUriUsingFirstParentTreeUri(recordingFile)
                createSAFFileSdk30(recordingFile)
                contentResolver.openFileDescriptor(fileUri, "w")!!
                    .use { recorder?.setOutputFile(it) }
            } else if (isPathOnSD(recordingFile)) {
                var document = getDocumentFile(recordingFile.getParentPath())
                document = document?.createFile("", recordingFile.getFilenameFromPath())
                contentResolver.openFileDescriptor(document!!.uri, "w")!!
                    .use { recorder?.setOutputFile(it) }
            } else {
                recorder?.setOutputFile(recordingFile)
            }*/

            recorder?.prepare()
            recorder?.start()
            duration = 0
            status = RECORDING_RUNNING
            broadcastRecorderInfo()
            startForeground(RECORDER_RUNNING_NOTIF_ID, showNotification())

            durationTimer = Timer()
            durationTimer.scheduleAtFixedRate(getDurationUpdateTask(), 1000, 1000)

            startAmplitudeUpdates()
        } catch (e: Exception) {
//            showErrorToast(e)
            stopRecording()
        }
    }

    private fun stopRecording() {
        durationTimer.cancel()
        amplitudeTimer.cancel()
        status = RECORDING_STOPPED

        recorder?.apply {
            try {
                stop()
                release()
            } catch (
                @Suppress(
                    "TooGenericExceptionCaught",
                    "SwallowedException"
                ) e: RuntimeException
            ) {
//                toast(R.string.recording_too_short)
            } catch (e: Exception) {
//                showErrorToast(e)
                e.printStackTrace()
            }

            GlobalScope.launch(Dispatchers.IO) {
//                scanRecording()
                EventBus.getDefault().post(Events.RecordingCompleted())
            }
        }
        recorder = null
    }

    private fun cancelRecording() {
        durationTimer.cancel()
        amplitudeTimer.cancel()
        status = RECORDING_STOPPED

        recorder?.apply {
            try {
                stop()
                release()
            } catch (ignored: Exception) {
            }
        }

        recorder = null
        /*if (isRPlus()) {
            val recordingUri = createDocumentUriUsingFirstParentTreeUri(recordingFile)
            DocumentsContract.deleteDocument(contentResolver, recordingUri)
        } else {
            File(recordingFile).delete()
        }*/

        EventBus.getDefault().post(Events.RecordingCompleted())
        stopSelf()
    }

    private fun broadcastRecorderInfo() {
        broadcastDuration()
        broadcastStatus()
        startAmplitudeUpdates()
    }

    @SuppressLint("DiscouragedApi")
    private fun startAmplitudeUpdates() {
        amplitudeTimer.cancel()
        amplitudeTimer = Timer()
        amplitudeTimer.scheduleAtFixedRate(getAmplitudeUpdateTask(), 0, AMPLITUDE_UPDATE_MS)
    }

    @SuppressLint("NewApi")
    private fun togglePause() {
        try {
            if (status == RECORDING_RUNNING) {
                recorder?.pause()
                status = RECORDING_PAUSED
            } else if (status == RECORDING_PAUSED) {
                recorder?.resume()
                status = RECORDING_RUNNING
            }
            broadcastStatus()
            startForeground(RECORDER_RUNNING_NOTIF_ID, showNotification())
        } catch (e: Exception) {
//            showErrorToast(e)
        }
    }

/*    private fun scanRecording() {
        MediaScannerConnection.scanFile(
            this,
            arrayOf(recordingFile),
            arrayOf(recordingFile.getMimeType())
        ) { _, uri ->
            if (uri == null) {
                toast(org.fossify.commons.R.string.unknown_error_occurred)
                return@scanFile
            }

            recordingSavedSuccessfully(uri)
        }
    }*/

    private fun recordingSavedSuccessfully(savedUri: Uri) {
//        toast(R.string.recording_saved_successfully)
        EventBus.getDefault().post(Events.RecordingSaved(savedUri))
    }

    private fun getDurationUpdateTask() = object : TimerTask() {
        override fun run() {
            if (status == RECORDING_RUNNING) {
                duration++
                broadcastDuration()
            }
        }
    }

    private fun getAmplitudeUpdateTask() = object : TimerTask() {
        override fun run() {
            if (recorder != null) {
                try {
                    EventBus.getDefault()
                        .post(Events.RecordingAmplitude(recorder!!.getMaxAmplitude()))
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private fun showNotification(): Notification {
        val channelId = "simple_recorder"
        val label = getString(R.string.app_name)
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(channelId, label, NotificationManager.IMPORTANCE_DEFAULT).apply {
                setSound(null, null)
                notificationManager.createNotificationChannel(this)
            }
        }

        val icon = R.drawable.earth
        val title = label
        val visibility = NotificationCompat.VISIBILITY_PUBLIC
        var text = getString(R.string.recording)
        if (status == RECORDING_PAUSED) {
            text += " (${getString(R.string.paused)})"
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(icon)
            .setContentIntent(getOpenAppIntent())
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setVisibility(visibility)
            .setSound(null)
            .setOngoing(true)
            .setAutoCancel(true)

        return builder.build()
    }

    private fun getOpenAppIntent(): PendingIntent {
        val intent =  Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            RECORDER_RUNNING_NOTIF_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun broadcastDuration() {
        EventBus.getDefault().post(Events.RecordingDuration(duration))
    }

    private fun broadcastStatus() {
        EventBus.getDefault().post(Events.RecordingStatus(status))
    }

    private fun recordMp3(): Boolean {
        return config.extension == EXTENSION_MP3
    }
}
