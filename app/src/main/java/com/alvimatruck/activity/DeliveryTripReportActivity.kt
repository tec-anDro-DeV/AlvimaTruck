package com.alvimatruck.activity

import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityDeliveryTripReportBinding

class DeliveryTripReportActivity : BaseActivity<ActivityDeliveryTripReportBinding>() {
    override fun inflateBinding(): ActivityDeliveryTripReportBinding {
        return ActivityDeliveryTripReportBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

    }
}