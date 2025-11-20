package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivitySalesRouteBinding

class SalesRouteActivity : BaseActivity<ActivitySalesRouteBinding>() {
    override fun inflateBinding(): ActivitySalesRouteBinding {
        return ActivitySalesRouteBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rlRoute.setOnClickListener {
            startActivity(Intent(this, TripRouteListActivity::class.java))
        }

        binding.rlSales.setOnClickListener {
            startActivity(Intent(this, SalesOrderListActivity::class.java))
        }


    }
}