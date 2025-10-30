package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.alvimatruck.BuildConfig
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivitySplashBinding
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.SharedHelper

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun inflateBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLogin = SharedHelper.getBoolKey(this, Constants.IS_LOGIN)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isLogin) {
                startActivity(Intent(this, OnBoardingActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, DemoActivity::class.java))
                finish()
            }

        }, 3000)

        binding.tvAppVersion.text = "Version " + BuildConfig.VERSION_NAME


    }
}