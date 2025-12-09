package com.example.capermintpractical.presentation.utility


import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.visualizer.amplitude.AudioRecordView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class AudioRecorderHelper(private val audioRecordView: AudioRecordView) {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var isPaused = false
    private var recordingJob: Job? = null

    // Standard high-quality config
    private val SAMPLE_RATE = 44100
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

    // Buffer size calculation is critical to avoid Error -20
    private val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

    @SuppressLint("MissingPermission") // Checked in Activity
    fun startRecording() {
        if (isRecording) return

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                // This catches the -20 error specifically
                throw IllegalStateException("AudioRecord failed to initialize")
            }

            audioRecord?.startRecording()
            isRecording = true
            isPaused = false

            // Start reading data in a background thread
            startVisualizerUpdate()

        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
        }
    }

    private fun startVisualizerUpdate() {
        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ShortArray(bufferSize)

            while (isActive && isRecording) {
                val readResult = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                if (readResult > 0) {
                    if (isPaused) continue

                    // Calculate amplitude for the visualizer
                    // Simple max amplitude approach for visualization
                    var maxAmplitude = 0
                    for (i in 0 until readResult) {
                        if (abs(buffer[i].toInt()) > maxAmplitude) {
                            maxAmplitude = abs(buffer[i].toInt())
                        }
                    }

                    // Update View on Main Thread
                    withContext(Dispatchers.Main) {
                        // The library expects 'update' to be called with the amplitude
                        audioRecordView.update(maxAmplitude)
                    }
                }
            }
        }
    }

    fun stopRecording() {
        isRecording = false
        isPaused = false
        recordingJob?.cancel()

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioRecord = null
            // Reset visualizer
            audioRecordView.recreate()
        }
    }

    fun pauseRecording() {
        isPaused = true
    }

    fun resumeRecording() {
        isPaused = false
    }

    fun isRecording() = isRecording

    fun isPaused() = isPaused
}
