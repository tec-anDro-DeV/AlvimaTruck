package com.alvimatruck.custom

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

object WebSocketManager {

    private var webSocket: WebSocket? = null
    private var isConnected = false

    private const val WS_URL = "ws://192.168.1.10:8080"

    private val client = OkHttpClient()

    fun connect() {

        if (isConnected) return

        val request = Request.Builder().url(WS_URL).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                isConnected = true
                println("✅ Connected")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                reconnect()
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                isConnected = false
                reconnect()
            }
        })
    }

    fun sendLocation(lat: Double, lon: Double) {

        if (!isConnected) {
            connect()
            return
        }

        val json = """{"lat":$lat,"lon":$lon}"""
        webSocket?.send(json)
    }

    private fun reconnect() {

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            connect()
        }, 3000)
    }

    fun disconnect() {
        webSocket?.close(1000, "Closed")
        isConnected = false
    }
}
