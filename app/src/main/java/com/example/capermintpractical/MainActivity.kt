package com.example.capermintpractical

import android.os.Bundle
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.capermintpractical.databinding.ActivityMainBinding
import com.example.capermintpractical.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        setupObservers()
        setupListeners()
        NightModeManager.applyNightMode(this)
    }

    private fun setupObservers() {
        viewModel.audioStatus.observe(this) { status ->
            binding.tvAudioStatus.text = status
        }

        viewModel.nightModeEnabled.observe(this) { enabled ->
            binding.switchNightMode.setOnCheckedChangeListener(null)
            binding.switchNightMode.isChecked = enabled
            setupNightModeSwitch()
        }

        viewModel.startTime.observe(this) { time ->
            binding.btnStartTime.text = time
        }

        viewModel.endTime.observe(this) { time ->
            binding.btnEndTime.text = time
        }

        viewModel.nightModeStatus.observe(this) { status ->
            binding.tvNightModeStatus.text = status
        }
    }

    private fun setupListeners() {
        binding.btnPlayAudio.setOnClickListener {
            viewModel.playAudio()
        }

        binding.btnStopAudio.setOnClickListener {
            viewModel.stopAudio()
        }

        setupNightModeSwitch()

        binding.btnStartTime.setOnClickListener {
            showTimePicker(true)
        }

        binding.btnEndTime.setOnClickListener {
            showTimePicker(false)
        }
    }

    private fun setupNightModeSwitch() {
        binding.switchNightMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNightModeEnabled(isChecked)
            delegate.applyDayNight()
        }
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val prefs = NightModeManager.loadSettings(this)
        val initialHour = if (isStartTime) prefs.startHour else prefs.endHour
        val initialMinute = if (isStartTime) prefs.startMinute else prefs.endMinute

        val timePicker = TimePicker(this).apply {
            hour = initialHour
            minute = initialMinute
            setIs24HourView(true)
        }

        AlertDialog.Builder(this)
            .setTitle(if (isStartTime) getString(R.string.start_time) else getString(R.string.end_time))
            .setView(timePicker)
            .setPositiveButton("OK") { _, _ ->
                val hour =
                    timePicker.hour
                val minute =
                    timePicker.minute
                viewModel.updateTime(isStartTime, hour, minute)
                delegate.applyDayNight()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateNightModeStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopAudio()
    }
}
