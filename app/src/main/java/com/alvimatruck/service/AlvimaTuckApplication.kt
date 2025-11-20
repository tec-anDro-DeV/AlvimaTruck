package com.alvimatruck.service

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat

class AlvimaTuckApplication : Application() {

    companion object {
        var locationService: LocationService? = null

        fun startLocationService(context: Context) {
            if (locationService != null) return // already started

            val intent = Intent(context, LocationService::class.java)
            ContextCompat.startForegroundService(context, intent)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
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
}