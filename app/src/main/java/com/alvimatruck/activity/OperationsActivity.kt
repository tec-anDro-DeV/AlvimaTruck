package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityOperationsBinding
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.SharedHelper

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
            if (SharedHelper.getBoolKey(this@OperationsActivity, Constants.DayStart)) {
                Toast.makeText(
                    this,
                    getString(R.string.stock_transfer_is_not_allowed_once_the_trip_has_started),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startActivity(Intent(this, TransferRequestActivity::class.java))
            }
        }

        binding.rlDeposit.setOnClickListener {
            startActivity(Intent(this, DepositActivity::class.java))
        }
        binding.rlStoreRequisition.setOnClickListener {
            if (SharedHelper.getBoolKey(this@OperationsActivity, Constants.DayStart)) {
                Toast.makeText(
                    this,
                    getString(R.string.store_requisition_is_not_allowed_once_the_trip_has_started),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startActivity(Intent(this, StoreRequisitionActivity::class.java))
            }
        }

    }
}