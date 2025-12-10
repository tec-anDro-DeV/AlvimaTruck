package com.alvimatruck.activity

import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityTripReportBinding

class TripReportActivity : BaseActivity<ActivityTripReportBinding>() {
    override fun inflateBinding(): ActivityTripReportBinding {
        return ActivityTripReportBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

    }
}