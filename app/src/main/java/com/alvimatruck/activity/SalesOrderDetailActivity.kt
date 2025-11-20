package com.alvimatruck.activity

import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivitySalesOrderDetailBinding

class SalesOrderDetailActivity : BaseActivity<ActivitySalesOrderDetailBinding>() {
    override fun inflateBinding(): ActivitySalesOrderDetailBinding {
        return ActivitySalesOrderDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }


    }
}