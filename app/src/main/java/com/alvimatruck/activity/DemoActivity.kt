package com.alvimatruck.activity

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityDemoBinding
import com.alvimatruck.service.LocationService


class DemoActivity : BaseActivity<ActivityDemoBinding>() {


    override fun inflateBinding(): ActivityDemoBinding {
        return ActivityDemoBinding.inflate(layoutInflater)
    }

    private var locationService: LocationService? = null
    private var isBound = false  // ✅ Add this flag


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as LocationService.LocalBinder
            locationService = localBinder.getService()
            isBound = true

            locationService?.setLocationCallback { location ->
                runOnUiThread {
                    binding.tvCurrentLocation.text =
                        "Lat: ${location.latitude}\nLon: ${location.longitude}"
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            locationService = null
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            try {
                unbindService(connection)
                isBound = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAllPermissionsAndStartService()

        binding.btnOpenMap.setOnClickListener {
            startActivity(Intent(this, MapPolygonActivity::class.java))
        }
        binding.btnOpenRoutePath.setOnClickListener {
            startActivity(Intent(this, MapRouteActivity::class.java))
        }

        binding.btnOpenList.setOnClickListener {
            startActivity(Intent(this, DemoListActivity::class.java))
        }
    }

    private fun checkAllPermissionsAndStartService() {
        when {
            !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || !hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 100
                )
            }

            !hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 101
                )
            }

            Build.VERSION.SDK_INT >= 34 && !hasPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION) -> {
                requestPermissions(
                    arrayOf(Manifest.permission.FOREGROUND_SERVICE_LOCATION), 102
                )
            }

            else -> {
                startAndBindService()
            }
        }
    }

    private fun startAndBindService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            // continue checking next level
            checkAllPermissionsAndStartService()
        } else {
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("Please grant location permissions to use this feature.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }.setNegativeButton("Cancel", null).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            try {
                unbindService(connection)
                isBound = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

