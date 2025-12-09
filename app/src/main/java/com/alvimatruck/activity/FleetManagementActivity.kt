package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
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
            val intent = Intent(this, FuelRefillRequestActivity::class.java)
            startForResult.launch(intent)
        }

        binding.rlRepairLog.setOnClickListener {
            val intent = Intent(this, RepairLogActivity::class.java)
            startForResult.launch(intent)
        }

        binding.rlIncidentReporting.setOnClickListener {
            val intent = Intent(this, IncidentReportingActivity::class.java)
            startForResult.launch(intent)
        }

        binding.tvSeeAll.setOnClickListener {
            startActivity(Intent(this, RecentLogsActivity::class.java))

        }

    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {

        }
    }
}