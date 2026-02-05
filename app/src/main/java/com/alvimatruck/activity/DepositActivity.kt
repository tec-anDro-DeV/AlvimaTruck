package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityDepositBinding

class DepositActivity : BaseActivity<ActivityDepositBinding>() {

    override fun inflateBinding(): ActivityDepositBinding {
        return ActivityDepositBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rlDeposit.setOnClickListener {
            startActivity(Intent(this, SendDepositActivity::class.java))
        }

        binding.rlPaymentLogs.setOnClickListener {
            startActivity(Intent(this, PaymentLogsActivity::class.java))
        }
    }
}