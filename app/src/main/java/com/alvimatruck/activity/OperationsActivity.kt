package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityOperationsBinding

class OperationsActivity : BaseActivity<ActivityOperationsBinding>() {
    override fun inflateBinding(): ActivityOperationsBinding {
        return ActivityOperationsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rlBottomHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }

        binding.rlBottomVanStock.setOnClickListener {
            startActivity(Intent(this, VanStockActivity::class.java))
        }

        binding.rlTransfer.setOnClickListener {
            startActivity(Intent(this, TransferRequestActivity::class.java))
        }

        binding.rlDeposit.setOnClickListener {
            startActivity(Intent(this, DepositActivity::class.java))
        }
        binding.rlStoreRequisition.setOnClickListener {
            startActivity(Intent(this, StoreRequisitionActivity::class.java))
        }

    }
}