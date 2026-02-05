package com.alvimatruck.custom

import android.os.Handler
import android.os.Looper
import com.alvimatruck.utils.Constants
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState

object SignalRManager {

    private var hubConnection: HubConnection? = null
    private var isConnecting = false


    private const val HUB_URL = Constants.IMAGE_URL + "live-tracking" // <-- your SignalR hub URL

    // ✅ Connect SignalR
    fun connect() {
        // ✅ already connected → do nothing
        if (hubConnection?.connectionState == HubConnectionState.CONNECTED)
            return

        // ✅ prevent parallel connects
        if (isConnecting) return
        isConnecting = true

        // ✅ build only once
        if (hubConnection == null) {

            hubConnection = HubConnectionBuilder
                .create(HUB_URL)
                .build()

            // ✅ auto reconnect on close
            hubConnection?.onClosed { error ->

                println("❌ SignalR Closed: ${error?.message}")

                isConnecting = false

                Handler(Looper.getMainLooper()).postDelayed({
                    println("🔄 Reconnecting SignalR...")
                    connect()
                }, 3000)
            }
        }

        // ✅ start connection
        hubConnection?.start()
            ?.doOnComplete {
                isConnecting = false
                println("✅ SignalR Connected")
            }
            ?.doOnError {
                isConnecting = false
                println("❌ SignalR Start Error: ${it.message}")
            }
            ?.subscribe()
    }

    // ✅ Send Location to .NET Hub
    fun sendLocation(driverId: String, lat: Double, lon: Double) {

        val conn = hubConnection ?: return

        if (conn.connectionState != HubConnectionState.CONNECTED) {
            println("⚠ SignalR not connected — reconnecting")
            connect()
            return
        }
        try {
            val payload = mapOf(
                "DriverNo" to driverId,
                "latitude" to lat,
                "longitude" to lon
            )

            conn.send("SendLocation", payload)

            println("✅ Location Sent: $lat,$lon")
        } catch (e: Exception) {
            println("❌ Send failed: ${e.message}")
        }
    }

    // ✅ Disconnect
    fun disconnect() {
        hubConnection?.stop()
        println("❌ SignalR Disconnected")
    }
}