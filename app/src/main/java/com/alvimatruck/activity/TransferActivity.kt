package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityTransferBinding

class TransferActivity : BaseActivity<ActivityTransferBinding>() {
    override fun inflateBinding(): ActivityTransferBinding {
        return ActivityTransferBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        binding.rlTransferRequestShip.setOnClickListener {
            startActivity(Intent(this, TransferRequestActivity::class.java))
        }

        binding.rlTransferShipToReceive.setOnClickListener {
            startActivity(Intent(this, TransferShipToReceiveActivity::class.java))
        }

        binding.rlTransferListing.setOnClickListener {
            startActivity(Intent(this, TransferShipAndReceiveActivity::class.java))

        }

    }
}