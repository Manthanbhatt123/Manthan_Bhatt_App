package com.example.capermintpractical.presentation.main

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.capermintpractical.NightModeManager
import com.example.capermintpractical.R
import com.example.capermintpractical.databinding.ActivityMainBinding
import com.example.capermintpractical.presentation.DynamicNanoHttpD.DynamicNanoHttpD
import com.example.capermintpractical.presentation.utility.setSafeOnClickListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.e("ImageUri", "Selected URI: $uri")
            binding.ivMedia.setImageURI(uri)
            // Handle the image here
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    private val takePicture = registerForActivityResult(TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            Log.d("Camera", "Photo taken successfully $bitmap")
            // Display the image. For example, showing it in your ImageView:
            binding.ivMedia.setImageBitmap(bitmap)
        } else {
            Log.d("Camera", "Camera cancelled")
        }
    }

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
//        setUpGIF(1)
//        setupGifLoopCount()
        setupAnimationGIF(2)

    }
    fun openInternalActivity(pkg: String, activity: String) {
        val intent = Intent().apply {
            setClassName(pkg, activity)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Activity not accessible", Toast.LENGTH_SHORT).show()
        }
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
        binding.btnPlayAudio.setSafeOnClickListener {
            viewModel.playAudio()
        }

        binding.btnStopAudio.setSafeOnClickListener {
            viewModel.stopAudio()
        }

        setupNightModeSwitch()

        binding.btnStartTime.setSafeOnClickListener {
            showTimePicker(true)
        }

        binding.btnEndTime.setSafeOnClickListener {
            showTimePicker(false)
        }

        binding.btnIntent.setSafeOnClickListener {
            openInternalActivity("com.app.baseStructure",
                "com.app.baseStructure.presentation.splash.SplashActivity")
        }

        binding.btnWebView.setSafeOnClickListener {
            startActivity(Intent(this, DynamicNanoHttpD::class.java))
        }

        binding.btnCamera.setSafeOnClickListener {
            // Only LAUNCH the intent here. Do not register here.
            takePicture.launch(null)
        }
        binding.btnGallery.setSafeOnClickListener {
            // Only LAUNCH the intent here. Do not register here.
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))

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
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.edtGifLoopCount.windowToken, 0)
    }


    private fun setupGifLoopCount() {
        binding.btnLoopCount.setOnClickListener {
            hideKeyboard()

            val countText = binding.edtGifLoopCount.text.toString()

            if (countText.isEmpty()) {
                Toast.makeText(this, "Enter a loop count", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loopCount = countText.toIntOrNull()
            if (loopCount == null || loopCount < 1) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.e("LoopCount", "setupGifLoopCount: $loopCount", )
            setUpGIF(loopCount)
        }

    }

    private fun setUpGIF(loopCount: Int) {
        Log.e("LoopCount===", "setupGifLoopCount: $loopCount", )

        Glide.with(this)
            .asGif()
            .load(R.drawable.earth)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    Toast.makeText(applicationContext, "Failed to load GIF", Toast.LENGTH_SHORT).show()
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable,
                    model: Any,
                    target: Target<GifDrawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("LoopCount++++", "setupGifLoopCount: $loopCount", )

                    resource.setLoopCount(loopCount)

                    val countedLoop: Animatable2Compat.AnimationCallback = object : Animatable2Compat.AnimationCallback(){
                        override fun onAnimationEnd(drawable: Drawable?) {
                            super.onAnimationEnd(drawable)
                            Toast.makeText(this@MainActivity, "Loops end here", Toast.LENGTH_SHORT).show()
                            resource.unregisterAnimationCallback(this)
                        }
                    }

                    resource.registerAnimationCallback(countedLoop)
                    return false
                }
            })
            .into(binding.iVGif)

    }
    private fun setupAnimationGIF(loopCount: Int = 1) {
        val animation = AnimationDrawable()
        val frameDuration = 50
        val totalFrames = 203
        // List files inside assets/airzoy
        val frameFiles = assets.list("airzoy") ?: emptyArray()

        val sortedFrames = frameFiles.sorted()
        for (fileName in sortedFrames) {
            val inputStream = assets.open("airzoy/$fileName")
            val drawable = Drawable.createFromStream(inputStream, null)

            if (drawable != null) {
                animation.addFrame(drawable, frameDuration)
            }

        }
        /*for (i in 0..totalFrames) {
            val name = "airzoy%03d".format(i)
            val id = resources.getIdentifier(name, "drawable", packageName)
            if (id != 0) animation.addFrame(resources.getDrawable(id, null), frameDuration)
        }*/

        binding.iVGif.setImageDrawable(animation)
        animation.isOneShot = true

        val totalDuration = animation.numberOfFrames * frameDuration
        var loop = 0

        binding.iVGif.post(object : Runnable {
            override fun run() {
                if (loop >= loopCount) return
                loop++

                animation.stop()
                animation.selectDrawable(0)
                animation.start()

                binding.iVGif.postDelayed(this, totalDuration.toLong())
            }
        })
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