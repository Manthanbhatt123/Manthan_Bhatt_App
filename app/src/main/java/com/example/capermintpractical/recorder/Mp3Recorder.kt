package com.example.capermintpractical.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.capermintpractical.presentation.utility.Config
//import com.example.capermintpractical.presentation.utility.config
//import com.naman14.androidlame.AndroidLame
//import com.naman14.androidlame.LameBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
//import org.fossify.commons.extensions.showErrorToast
//import org.fossify.commons.helpers.ensureBackgroundThread
//import org.fossify.voicerecorder.extensions.config
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

class Mp3Recorder(val context: Context) : Recorder {
    private var mp3buffer: ByteArray = ByteArray(0)
    private var isPaused = AtomicBoolean(false)
    private var isStopped = AtomicBoolean(false)
    private var amplitude = AtomicInteger(0)
    private var outputPath: String? = null
//    private var androidLame: AndroidLame? = null
    private val config = Config(context)
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var outputStream: FileOutputStream? = null
    private val minBufferSize = AudioRecord.getMinBufferSize(
        config.samplingRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    @SuppressLint("MissingPermission")
    val audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        48000,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        minBufferSize
    )

    override fun setOutputFile(path: String) {
        outputPath = path
    }

    override fun prepare() {}

    override fun start() {
        val rawData = ShortArray(minBufferSize)
        mp3buffer = ByteArray((7200 + rawData.size * 2 * 1.25).toInt())
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e("RecorderBufferSize", "start: $minBufferSize", )        }
        outputStream = try {
            if (fileDescriptor != null) {
                FileOutputStream(fileDescriptor!!.fileDescriptor)
            } else {
                FileOutputStream(File(outputPath!!))
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return
        }

       /* androidLame = LameBuilder()
            .setInSampleRate(context.config.samplingRate)
            .setOutBitrate(context.config.bitrate / 1000)
            .setOutSampleRate(context.config.samplingRate)
            .setOutChannels(1)
            .build()*/

        GlobalScope.launch(Dispatchers.IO) {
            try {
                audioRecord.startRecording()
            } catch (e: Exception) {
//                context.showErrorToast(e)
//                return@ensureBackgroundThread
            }

            while (!isStopped.get()) {
                if (!isPaused.get()) {
                    val count = audioRecord.read(rawData, 0, minBufferSize)
                    if (count > 0) {
                        updateAmplitude(rawData)
                        /*val encoded = androidLame!!.encode(rawData, rawData, count, mp3buffer)
                        if (encoded > 0) {
                            try {
                                updateAmplitude(rawData)
                                outputStream!!.write(mp3buffer, 0, encoded)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }*/
                    }
                }
            }
        }
    }

    override fun stop() {
        isPaused.set(true)
        isStopped.set(true)
        audioRecord.stop()
    }

    override fun pause() {
        isPaused.set(true)
    }

    override fun resume() {
        isPaused.set(false)
    }

    override fun release() {
//        androidLame?.flush(mp3buffer)
        outputStream?.close()
        audioRecord.release()
    }

    override fun getMaxAmplitude(): Int {
        return amplitude.get()
    }

    override fun setOutputFile(parcelFileDescriptor: ParcelFileDescriptor) {
        this.fileDescriptor = ParcelFileDescriptor.dup(parcelFileDescriptor.fileDescriptor)
    }

    private fun updateAmplitude(data: ShortArray) {
        var sum = 0L
        for (i in 0 until minBufferSize step 2) {
            sum += abs(data[i].toInt())
        }
        amplitude.set((sum / (minBufferSize / 8)).toInt())
    }
}
