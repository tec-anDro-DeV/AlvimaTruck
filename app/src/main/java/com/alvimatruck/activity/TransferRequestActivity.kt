package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityTransferRequestBinding

class TransferRequestActivity : BaseActivity<ActivityTransferRequestBinding>() {
    override fun inflateBinding(): ActivityTransferRequestBinding {
        return ActivityTransferRequestBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        binding.rlCreateTransferRequest.setOnClickListener {
            startActivity(Intent(this, CreateTransferRequestActivity::class.java))
        }
        binding.rlTransferRequestList.setOnClickListener {
            startActivity(Intent(this, TransferOrderListActivity::class.java))
        }


    }
}