package com.example.capermintpractical.presentation.nanohttpd

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.capermintpractical.R
import com.example.capermintpractical.databinding.ActivityNanoHttpDactivityBinding
import com.example.capermintpractical.presentation.utility.setSafeOnClickListener
import com.example.capermintpractical.server.MyHttpServer

class NanoHttpDActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNanoHttpDactivityBinding

    private var server: MyHttpServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNanoHttpDactivityBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setServer()
        setWebView()
        setupListener()

    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stopServer()
    }

    private fun setServer(){
        server = MyHttpServer(this, 8686)
        server?.startServer()
    }


    private fun setWebView(){
        // Avoid opening external browser
        binding.webView.webViewClient = WebViewClient()
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true

        // 3. Load local server URL
        binding.webView.loadUrl("http://localhost:8686")
    }

    private fun setupListener(){
        binding.btnBack.setSafeOnClickListener {
            finish()
        }
    }

}