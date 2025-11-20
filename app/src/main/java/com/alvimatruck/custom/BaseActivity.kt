package com.alvimatruck.custom

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.alvimatruck.service.AlvimaTuckApplication

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
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            !hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 100
            )
        } else if (Build.VERSION.SDK_INT >= 29 &&
            !hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 101
            )
        } else if (Build.VERSION.SDK_INT >= 34 &&
            !hasPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.FOREGROUND_SERVICE_LOCATION), 102
            )
        } else {
            AlvimaTuckApplication.startLocationService(this)   // ✔ start only once
        }
    }

    private fun hasPermission(perm: String): Boolean =
        ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            checkAndStartLocationService()
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show()
        }
    }
}
