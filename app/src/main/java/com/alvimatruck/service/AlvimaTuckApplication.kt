package com.alvimatruck.service

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.alvimatruck.custom.SignalRManager

class AlvimaTuckApplication : Application() {

    companion object {
        lateinit var instance: AlvimaTuckApplication
        var locationService: LocationService? = null

        var latitude: Double = 0.0
        var longitude: Double = 0.0

        fun startLocationService(context: Context) {
            if (!hasLocationPermission(context)) {
                println("❌ Skip start — no permission")
                return
            }

            if (locationService != null) return // already started

            val intent = Intent(context, LocationService::class.java)
            ContextCompat.startForegroundService(context, intent)
            context.bindService(intent, connection, BIND_AUTO_CREATE)
        }

        fun hasLocationPermission(ctx: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        ctx, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
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

        // SocketManager.connect()
        //WebSocketManager.connect()
        SignalRManager.connect()
    }

    fun stopLocationService(context: Context) {

        try {
            locationService?.stopSelf()
            context.unbindService(connection)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        locationService = null
    }
}