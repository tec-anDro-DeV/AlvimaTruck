package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityFleetManagementBinding

class FleetManagementActivity : BaseActivity<ActivityFleetManagementBinding>() {
    override fun inflateBinding(): ActivityFleetManagementBinding {
        return ActivityFleetManagementBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rlFuelRefillRequest.setOnClickListener {
            startActivity(Intent(this, FuelRefillRequestActivity::class.java))
        }

        binding.rlRepairLog.setOnClickListener {
            startActivity(Intent(this, RepairLogActivity::class.java))
        }

        binding.rlIncidentReporting.setOnClickListener {
            startActivity(Intent(this, IncidentReportingActivity::class.java))
        }

    }
}