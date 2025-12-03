package com.example.capermintpractical.server

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import fi.iki.elonen.NanoHTTPD
import java.io.IOException

class MyHttpServer(private val context: Context, port: Int) : NanoHTTPD(port) {
    fun startServer() {
        try {
            start(SOCKET_READ_TIMEOUT, false)
            Log.d("MyHttpServer", "Server started at http://localhost:$listeningPort")
        } catch (e: Exception) {
            Log.e("MyHttpServer", "Error starting server: ${e.message}")
        }
    }

    fun stopServer() {
        stop()
        Log.d("MyHttpServer", "Server stopped.")
    }
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri.trimStart('/')

        // Default file = index.html
        val path = if (uri.isEmpty()) "www/index.html" else "www/$uri"

        return try {
            val inputStream = context.assets.open(path)
            val mimeType = getMimeTypeFromPath(path)
            val length = inputStream.available().toLong()

            newFixedLengthResponse(Response.Status.OK, mimeType, inputStream, length)

        } catch (e: IOException) {
            // Return 404 if file not found
            newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found")
        }
    }

    // Detect mime type using file extension
    private fun getMimeTypeFromPath(path: String): String {
        val extension = path.substringAfterLast('.', "")
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return mime ?: when (extension) {
            "js" -> "application/javascript"
            "css" -> "text/css"
            "svg" -> "image/svg+xml"
            "webm" -> "video/webm"
            "mp4" -> "video/mp4"
            "json" -> "application/json"
            else -> "application/octet-stream"
        }
    }

   /* override fun serve(session: IHTTPSession?): Response? {
        val html = """
            <html>
                <body>
                    <h1>Hello from Kotlin + NanoHTTPD</h1>
                </body>
            </html>
        """.trimIndent()

        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
//        return super.serve(session)
    }*/
}