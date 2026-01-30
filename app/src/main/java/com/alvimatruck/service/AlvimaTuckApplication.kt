package com.alvimatruck.service

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.alvimatruck.custom.SocketManager
import com.alvimatruck.custom.WebSocketManager

class AlvimaTuckApplication : Application() {

    companion object {
        lateinit var instance: AlvimaTuckApplication
        var locationService: LocationService? = null

        var latitude: Double = 0.0
        var longitude: Double = 0.0

        fun startLocationService(context: Context) {
            if (locationService != null) return // already started

            val intent = Intent(context, LocationService::class.java)
            ContextCompat.startForegroundService(context, intent)
            context.bindService(intent, connection, BIND_AUTO_CREATE)
        }


        private val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as LocationService.LocalBinder
                locationService = binder.getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                locationService = null
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this   // <-- MAKE INSTANCE AVAILABLE GLOBALLY

        SocketManager.connect()
        WebSocketManager.connect()
    }
}