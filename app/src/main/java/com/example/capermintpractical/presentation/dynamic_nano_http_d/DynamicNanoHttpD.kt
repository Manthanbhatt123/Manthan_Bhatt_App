package com.example.capermintpractical.presentation.dynamic_nano_http_d

import android.os.Bundle
import android.util.Log
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.capermintpractical.R
import com.example.capermintpractical.databinding.ActivityDynamicNanoHttpDBinding
import com.example.capermintpractical.server.FileHttpServer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream

class DynamicNanoHttpD : AppCompatActivity() {

    private lateinit var binding: ActivityDynamicNanoHttpDBinding
    private var server: FileHttpServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDynamicNanoHttpDBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupDynamicData()
    }

    private fun setupDynamicData() {
//        val dynamicData = intent.getStringExtra("dynamicData")
        val dynamicData = "https://www.dropbox.com/scl/fi/wenxpc4iowchkjkcqga1x/pose-hand-detection.zip?rlkey=3vmrzo2xi4umv5087p1yaayqb&e=1&st=ig5ga4im&dl=1"
        downloadZip(dynamicData)
    }

    private fun downloadZip(dynamicData: String?) {
        Thread{
            try {
                val connection = URL(dynamicData).openConnection()
                val fileLength = connection.contentLength

                val input = connection.getInputStream()

                val zipFile = File(filesDir,"webApp.zip")
                val output = FileOutputStream(zipFile)

                val data = ByteArray(1024)
                var total: Long = 0
                var count: Int

                while (input.read(data).also { count = it } != -1) {
                    total += count
                    output.write(data, 0, count)

                    val progress = ((total * 100) / fileLength).toInt()

                    runOnUiThread {
                        Log.e("DownloadZip", "downloadZip: $progress / 100", )
                    }
                }

                input.copyTo(output)
                output.close()
                input.close()

                runOnUiThread {
                    Toast.makeText(this, "Zip Downloaded", Toast.LENGTH_SHORT).show()
                }

                extractZip(zipFile)

            }catch (e: Exception){
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun extractZip(zipFile: File){
        val folder = File(filesDir, "webApp")
        if(folder.exists()){
            folder.deleteRecursively()
        }

        folder.mkdirs()

        Thread{
            try {
                ZipInputStream(FileInputStream(zipFile)).use {zip ->
                    var entry = zip.nextEntry
                    while (entry != null){
                        val file = File(folder, entry.name)
                        if(entry.isDirectory){
                            file.mkdirs()
                        } else {
                            file.parentFile?.mkdirs()
                            file.outputStream().use {output ->
                                zip.copyTo(output)
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                        }
                    }
                    runOnUiThread {
                        Toast.makeText(this, "Zip Extracted", Toast.LENGTH_SHORT).show()
                        startLocalServer(folder)
                }
            }catch (e: Exception){
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()

    }


    private fun startLocalServer(folder: File){
        server = FileHttpServer(folder,8686)
        server?.startServer()
        binding.webView.webViewClient = WebViewClient()
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.loadUrl("http://localhost:8686")
    }


    override fun onDestroy() {
        super.onDestroy()
        server?.stopServer()
    }

}