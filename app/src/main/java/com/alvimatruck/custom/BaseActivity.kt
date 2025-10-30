package com.alvimatruck.custom

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding

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
}
