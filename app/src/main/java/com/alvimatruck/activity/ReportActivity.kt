package com.alvimatruck.activity

import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityReportBinding

class ReportActivity : BaseActivity<ActivityReportBinding>() {
    override fun inflateBinding(): ActivityReportBinding {
        return ActivityReportBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
    }
}