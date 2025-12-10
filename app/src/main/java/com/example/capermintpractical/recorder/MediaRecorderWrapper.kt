/*
package com.example.capermintpractical.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.os.ParcelFileDescriptor
import com.example.capermintpractical.presentation.utility.Config

class MediaRecorderWrapper(val context: Context) : Recorder {
    val config = Config(context)
    @Suppress("DEPRECATION")
    private var recorder = MediaRecorder().apply {
        setAudioSource(config.microphoneMode)
        setOutputFormat(config.getOutputFormat())
        setAudioEncoder(config.getAudioEncoder())
        setAudioEncodingBitRate(config.bitrate)
        setAudioSamplingRate(config.samplingRate)
    }

    override fun setOutputFile(path: String) {
        recorder.setOutputFile(path)
    }

    override fun setOutputFile(parcelFileDescriptor: ParcelFileDescriptor) {
        val pFD = ParcelFileDescriptor.dup(parcelFileDescriptor.fileDescriptor)
        recorder.setOutputFile(pFD.fileDescriptor)
    }

    override fun prepare() {
        recorder.prepare()
    }

    override fun start() {
        recorder.start()
    }

    override fun stop() {
        recorder.stop()
    }

    @SuppressLint("NewApi")
    override fun pause() {
        recorder.pause()
    }

    @SuppressLint("NewApi")
    override fun resume() {
        recorder.resume()
    }

    override fun release() {
        recorder.release()
    }

    override fun getMaxAmplitude(): Int {
        return recorder.maxAmplitude
    }
}
*/
