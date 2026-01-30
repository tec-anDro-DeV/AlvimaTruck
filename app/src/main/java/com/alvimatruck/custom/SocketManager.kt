package com.alvimatruck.custom

import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

object SocketManager {

    private var socket: Socket? = null
    private var writer: PrintWriter? = null

    private const val SERVER_IP = "192.168.1.10" // change server IP
    private const val SERVER_PORT = 9999         // change port


    private var isConnecting = false

    // ✅ Check Socket Status
    private fun isSocketAlive(): Boolean {
        return socket != null &&
                socket!!.isConnected &&
                !socket!!.isClosed
    }

    // ✅ Auto Connect / Reconnect
    fun connect() {
        if (isSocketAlive()) return
        if (isConnecting) return

        isConnecting = true

        thread {
            try {
                println("🔄 Connecting Socket...")

                socket = Socket(SERVER_IP, SERVER_PORT)
                writer = PrintWriter(socket!!.getOutputStream(), true)

                println("✅ Socket Connected Successfully")

            } catch (e: Exception) {
                println("❌ Socket Connection Failed: ${e.message}")
            } finally {
                isConnecting = false
            }
        }
    }

    // ✅ Auto Send Location with Reconnect Support
    fun sendLocation(lat: Double, lon: Double) {

        thread {
            try {
                // ✅ Reconnect if socket not alive
                if (!isSocketAlive()) {
                    connect()
                    Thread.sleep(500) // wait small time for connection
                }

                val message = "LAT=$lat,LON=$lon"
                writer?.println(message)

                println("✅ Sent Location: $message")

            } catch (e: Exception) {
                println("❌ Send Failed: ${e.message}")
                reconnect()
            }
        }
    }

    // ✅ Reconnect Socket (If Broken)
    private fun reconnect() {
        disconnect()
        connect()
    }

    // ✅ Disconnect Cleanly
    fun disconnect() {
        try {
            writer?.close()
            socket?.close()
            writer = null
            socket = null

            println("❌ Socket Disconnected")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}