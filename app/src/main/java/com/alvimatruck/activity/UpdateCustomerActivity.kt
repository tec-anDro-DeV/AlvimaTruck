package com.alvimatruck.activity

import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityUpdateCustomerBinding

class UpdateCustomerActivity : BaseActivity<ActivityUpdateCustomerBinding>() {
    override fun inflateBinding(): ActivityUpdateCustomerBinding {
        return ActivityUpdateCustomerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.tvUpdate.setOnClickListener {
            handleBackPressed()
        }

        binding.tvCancel.setOnClickListener {
            handleBackPressed()
        }
    }
}