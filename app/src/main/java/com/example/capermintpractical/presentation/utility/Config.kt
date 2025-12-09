package com.example.capermintpractical.presentation.utility

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import androidx.core.content.edit
import com.example.capermintpractical.NightModeManager.PREFS_NAME
import com.example.capermintpractical.R

class Config(val context: Context)  /*:BaseConfig(context)*/ {
    companion object {

        fun newInstance(context: Context) = Config(context)
    }
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /*var saveRecordingsFolder: String
        get() = prefs.getString(SAVE_RECORDINGS, context.getDefaultRecordingsFolder())!!
        set(saveRecordingsFolder) = prefs.edit {
            putString(SAVE_RECORDINGS, saveRecordingsFolder)
        }*/

    var extension: Int
        get() = prefs.getInt(EXTENSION, EXTENSION_M4A)
        set(extension) = prefs.edit { putInt(EXTENSION, extension) }

    var microphoneMode: Int
        get() = prefs.getInt(MICROPHONE_MODE, MediaRecorder.AudioSource.DEFAULT)
        set(audioSource) = prefs.edit { putInt(MICROPHONE_MODE, audioSource) }

    /*fun getMicrophoneModeText(mode: Int) = context.getString(
        when (mode) {
            MediaRecorder.AudioSource.CAMCORDER -> R.string.microphone_mode_camcorder
            MediaRecorder.AudioSource.VOICE_COMMUNICATION -> R.string.microphone_mode_voice_communication
            MediaRecorder.AudioSource.VOICE_PERFORMANCE -> R.string.microphone_mode_voice_performance
            MediaRecorder.AudioSource.VOICE_RECOGNITION -> R.string.microphone_mode_voice_recognition
            MediaRecorder.AudioSource.UNPROCESSED -> R.string.microphone_mode_unprocessed
            else -> org.fossify.commons.R.string.system_default
        }
    )*/

    var bitrate: Int
        get() = prefs.getInt(BITRATE, DEFAULT_BITRATE)
        set(bitrate) = prefs.edit { putInt(BITRATE, bitrate) }

    var samplingRate: Int
        get() = prefs.getInt(SAMPLING_RATE, DEFAULT_SAMPLING_RATE)
        set(samplingRate) = prefs.edit { putInt(SAMPLING_RATE, samplingRate) }

    var recordAfterLaunch: Boolean
        get() = prefs.getBoolean(RECORD_AFTER_LAUNCH, false)
        set(recordAfterLaunch) = prefs.edit {
            putBoolean(RECORD_AFTER_LAUNCH, recordAfterLaunch)
        }

    /*fun getExtensionText() = context.getString(
        when (extension) {
            EXTENSION_M4A -> R.string.m4a
            EXTENSION_OGG -> R.string.ogg_opus
            else -> R.string.mp3_experimental
        }
    )*/

    fun getExtension() = context.getString(
        when (extension) {
            EXTENSION_M4A -> R.string.m4a
            EXTENSION_OGG -> R.string.ogg
            else -> R.string.mp3
        }
    )

    @SuppressLint("InlinedApi")
    fun getOutputFormat() = when (extension) {
        EXTENSION_OGG -> MediaRecorder.OutputFormat.OGG
        else -> MediaRecorder.OutputFormat.MPEG_4
    }

    @SuppressLint("InlinedApi")
    fun getAudioEncoder() = when (extension) {
        EXTENSION_OGG -> MediaRecorder.AudioEncoder.OPUS
        else -> MediaRecorder.AudioEncoder.AAC
    }

    var useRecycleBin: Boolean
        get() = prefs.getBoolean(USE_RECYCLE_BIN, true)
        set(useRecycleBin) = prefs.edit { putBoolean(USE_RECYCLE_BIN, useRecycleBin) }

    var lastRecycleBinCheck: Long
        get() = prefs.getLong(LAST_RECYCLE_BIN_CHECK, 0L)
        set(lastRecycleBinCheck) = prefs.edit {
            putLong(LAST_RECYCLE_BIN_CHECK, lastRecycleBinCheck)
        }

    var keepScreenOn: Boolean
        get() = prefs.getBoolean(KEEP_SCREEN_ON, true)
        set(keepScreenOn) = prefs.edit { putBoolean(KEEP_SCREEN_ON, keepScreenOn) }

    var wasMicModeWarningShown: Boolean
        get() = prefs.getBoolean(WAS_MIC_MODE_WARNING_SHOWN, false)
        set(wasMicModeWarningShown) = prefs.edit {
            putBoolean(WAS_MIC_MODE_WARNING_SHOWN, wasMicModeWarningShown)
        }
}
