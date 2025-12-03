package com.example.capermintpractical.presentation.main

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.capermintpractical.NightModeManager
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    // LiveData for UI updates
    private val _audioStatus = MutableLiveData<String>()
    val audioStatus: LiveData<String> = _audioStatus

    private val _nightModeEnabled = MutableLiveData<Boolean>()
    val nightModeEnabled: LiveData<Boolean> = _nightModeEnabled

    private val _startTime = MutableLiveData<String>()
    val startTime: LiveData<String> = _startTime

    private val _endTime = MutableLiveData<String>()
    val endTime: LiveData<String> = _endTime

    private val _nightModeStatus = MutableLiveData<String>()
    val nightModeStatus: LiveData<String> = _nightModeStatus

    // Audio components
    private var mediaPlayer: MediaPlayer? = null
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    // Sensor components
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    init {
        setupAudioFocus()
        loadNightModeSettings()
    }

    private fun setupAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            @Suppress("NewApi")
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener { focusChange ->
                    handleAudioFocusChange(focusChange)
                }
                .build()
        }
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaPlayer?.pause()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.start()
            }
        }
    }

    fun loadNightModeSettings() {
        val prefs = NightModeManager.loadSettings(getApplication())
        _nightModeEnabled.value = prefs.enabled
        _startTime.value = String.Companion.format(Locale.getDefault(), "%02d:%02d", prefs.startHour, prefs.startMinute)
        _endTime.value = String.Companion.format(Locale.getDefault(), "%02d:%02d", prefs.endHour, prefs.endMinute)
        updateNightModeStatus()
    }

    fun playAudio() {
        if (mediaPlayer?.isPlaying == true) return

        val audioFocusResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }

        if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            proximitySensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
            }

            val rawResId = getApplication<Application>().resources.getIdentifier("sample_audio", "raw", getApplication<Application>().packageName)
            if (rawResId == 0) {
                _audioStatus.value = "Audio file not found"
                return
            }

            try {
                mediaPlayer = MediaPlayer.create(getApplication(), rawResId)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                    isLooping = true
                    start()
                }
                // Initialize to speaker mode
                isEarpieceMode = false
                audioManager.mode = AudioManager.MODE_NORMAL
                audioManager.isSpeakerphoneOn = true
                _audioStatus.value = "Speaker Mode"
            } catch (e: Exception) {
                _audioStatus.value = "Error: ${e.message}"
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }

        sensorManager.unregisterListener(this)
        isEarpieceMode = false
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = true
        _audioStatus.value = "Speaker Mode"
    }

    private var isEarpieceMode = false

    private fun setEarpieceMode() {
        if (isEarpieceMode) return
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = false
        isEarpieceMode = true
        _audioStatus.postValue("Earpiece Mode")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY && mediaPlayer?.isPlaying == true) {
            val distance = event.values[0]
            val maxRange = event.sensor.maximumRange
            val isNear = distance < maxRange && distance < 5.0f
            if (isNear) {
                setEarpieceMode()
            } else {
                audioManager.mode = AudioManager.MODE_NORMAL
                audioManager.isSpeakerphoneOn = true
                isEarpieceMode = false
                _audioStatus.value = "Speaker Mode"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @Suppress("DEPRECATION")
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        handleAudioFocusChange(focusChange)
    }

    fun setNightModeEnabled(enabled: Boolean) {
        NightModeManager.saveSettings(getApplication(), enabled)
        _nightModeEnabled.value = enabled
        applyNightMode()
    }

    fun updateTime(isStartTime: Boolean, hour: Int, minute: Int) {
        val prefs = NightModeManager.loadSettings(getApplication())
        if (isStartTime) {
            NightModeManager.saveSettings(getApplication(), prefs.enabled, hour, minute, prefs.endHour, prefs.endMinute)
            _startTime.value = String.Companion.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        } else {
            NightModeManager.saveSettings(getApplication(), prefs.enabled, prefs.startHour, prefs.startMinute, hour, minute)
            _endTime.value = String.Companion.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        }
        applyNightMode()
    }

    fun applyNightMode() {
        if (_nightModeEnabled.value == true) {
            NightModeManager.applyNightMode(getApplication())
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        updateNightModeStatus()
    }

    fun updateNightModeStatus() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        val isNightActive = when (currentMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                val nightModeFlags = getApplication<Application>().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            }
            else -> false
        }
        _nightModeStatus.value = if (isNightActive) "Night Mode Active" else "Day Mode Active"
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        sensorManager.unregisterListener(this)
    }
}