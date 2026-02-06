package com.alvimatruck.custom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.alvimatruck.R
import com.alvimatruck.service.AlvimaTuckApplication
import com.alvimatruck.utils.Constants

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: T

    // Function that each subclass will implement to inflate its own binding
    protected abstract fun inflateBinding(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate and set the layout
        binding = inflateBinding()
        setContentView(binding.root)

        // Apply edge-to-edge padding automatically
        applyEdgeToEdgePadding()

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = false // false = white icons

        onBackPressedDispatcher.addCallback(this) {
            handleBackPressed(this)
        }
    }

    // Shared logic for back press across all screens
    open fun handleBackPressed(callback: OnBackPressedCallback? = null) {
        // Toast.makeText(this, "Back pressed performed (BaseActivity)", Toast.LENGTH_SHORT).show()
        Log.d("TAG", "handleBackPressed: " + this.localClassName)

        // Disable to prevent recursion and call default system back
        callback?.isEnabled = false
        onBackPressedDispatcher.onBackPressed()
    }

    private fun applyEdgeToEdgePadding() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun checkAndStartLocationService() {

        // 1️⃣ Location permission not granted → Ask
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) && !hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), Constants.LocationPermissionCode
            )
            return
        }

        // 2️⃣ Background permission (Android 10+)
        if (!hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                Constants.BackgroundPermissionCode
            )
            return
        }

        if (Build.VERSION.SDK_INT >= 33 && !hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                Constants.NotificationPermissionCode
            )
            return
        }

        // 3️⃣ Special foreground location service permission (Android 14+)
//        if (Build.VERSION.SDK_INT >= 34 && !hasPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION)) {
//            requestPermissions(
//                arrayOf(Manifest.permission.FOREGROUND_SERVICE_LOCATION),
//                Constants.ForgroundPermissionCode
//            )
//            return
//        }

        // 4️⃣ All location permissions granted → Now check battery optimization
        checkBatteryOptimization()  // 🔥 CRITICAL FOR ANDROID 14+


        // 5️⃣ Finally start tracking service only once
        AlvimaTuckApplication.startLocationService(this)
    }

    private fun checkBatteryOptimization() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager

        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            showBatteryOptimizationDialog()
        }
    }

    private fun showBatteryOptimizationDialog() {
        AlertDialog.Builder(this).setTitle(getString(R.string.allow_background_location_access))
            .setMessage(getString(R.string.to_track_location_offline_set_battery_unrestricted_for_this_app))
            .setPositiveButton("Open Settings") { _, _ ->
                openBatterySettings()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun openBatterySettings() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_SETTINGS)) // fallback
        }
    }

    private fun hasPermission(perm: String): Boolean =
        ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                checkAndStartLocationService()
            }, 1000) // Wait 1 second after user clicks "Allow"
        } else {
            Toast.makeText(
                this, getString(R.string.location_permission_required), Toast.LENGTH_LONG
            ).show()
        }
    }
}
