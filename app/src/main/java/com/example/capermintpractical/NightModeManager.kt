package com.example.capermintpractical

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import java.util.Calendar
import androidx.core.content.edit

data class NightModeSettings(
    val enabled: Boolean,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
)

object NightModeManager {
    const val PREFS_NAME = "night_mode_prefs"
    private const val KEY_ENABLED = "night_mode_enabled"
    private const val KEY_START_HOUR = "start_hour"
    private const val KEY_START_MINUTE = "start_minute"
    private const val KEY_END_HOUR = "end_hour"
    private const val KEY_END_MINUTE = "end_minute"

    // Default schedule: 18:00 to 08:00
    private const val DEFAULT_START_HOUR = 18
    private const val DEFAULT_START_MINUTE = 0
    private const val DEFAULT_END_HOUR = 8
    private const val DEFAULT_END_MINUTE = 0

    fun loadSettings(context: Context): NightModeSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return NightModeSettings(
            enabled = prefs.getBoolean(KEY_ENABLED, false),
            startHour = prefs.getInt(KEY_START_HOUR, DEFAULT_START_HOUR),
            startMinute = prefs.getInt(KEY_START_MINUTE, DEFAULT_START_MINUTE),
            endHour = prefs.getInt(KEY_END_HOUR, DEFAULT_END_HOUR),
            endMinute = prefs.getInt(KEY_END_MINUTE, DEFAULT_END_MINUTE)
        )
    }

    fun saveSettings(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_ENABLED, enabled)
        }
    }

    fun saveSettings(
        context: Context,
        enabled: Boolean,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_ENABLED, enabled)
            putInt(KEY_START_HOUR, startHour)
            putInt(KEY_START_MINUTE, startMinute)
            putInt(KEY_END_HOUR, endHour)
            putInt(KEY_END_MINUTE, endMinute)
        }
    }

    fun isNightModeActive(context: Context): Boolean {
        val settings = loadSettings(context)
        if (!settings.enabled) {
            return false
        }

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTimeMinutes = currentHour * 60 + currentMinute
        
        val startTimeMinutes = settings.startHour * 60 + settings.startMinute
        val endTimeMinutes = settings.endHour * 60 + settings.endMinute

        return if (startTimeMinutes > endTimeMinutes) {
            currentTimeMinutes !in endTimeMinutes..<startTimeMinutes
        } else {
            currentTimeMinutes in startTimeMinutes..<endTimeMinutes
        }
    }

    fun applyNightMode(context: Context) {
        val settings = loadSettings(context)
        
        if (!settings.enabled) {
            // If disabled, use system default
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            return
        }

        val isActive = isNightModeActive(context)
        AppCompatDelegate.setDefaultNightMode(
            if (isActive) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
