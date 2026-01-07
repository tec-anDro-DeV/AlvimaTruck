package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityReportBinding
import com.alvimatruck.utils.Constants

class ReportActivity : BaseActivity<ActivityReportBinding>() {
    override fun inflateBinding(): ActivityReportBinding {
        return ActivityReportBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rlTripReport.setOnClickListener {
            startActivity(Intent(this, TripReportActivity::class.java))
        }
        binding.rlStockReport.setOnClickListener {
            startActivity(
                Intent(this, VanStockActivity::class.java).putExtra(
                    Constants.IS_HIDE, true
                )
            )
        }
        binding.rlSalesReport.setOnClickListener {
            startActivity(Intent(this, SalesReportActivity::class.java))
        }


    }
}