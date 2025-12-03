package com.example.capermintpractical.server

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class FileHttpServer(
    private val rootDir: File,
    port: Int
) : NanoHTTPD("0.0.0.0", port) {
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

        var uri = session.uri.trimStart('/')
        if (uri.isEmpty()) uri = "index.html"

        val requestedFile = File(rootDir, uri)

        if (!requestedFile.exists()) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found")
        }

        val mime = getMimeType(uri)
        val input = FileInputStream(requestedFile)

        return newChunkedResponse(Response.Status.OK, mime, input)
    }

    private fun getMimeType(path: String): String {
        val ext = path.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "html" -> "text/html"
            "js" -> "application/javascript"
            "css" -> "text/css"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "svg" -> "image/svg+xml"
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "json" -> "application/json"
            else -> "application/octet-stream"
        }
    }
}
