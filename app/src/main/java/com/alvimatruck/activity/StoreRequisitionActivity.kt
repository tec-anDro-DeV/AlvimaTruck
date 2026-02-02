package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityStoreRequisitionBinding

class StoreRequisitionActivity : BaseActivity<ActivityStoreRequisitionBinding>() {
    override fun inflateBinding(): ActivityStoreRequisitionBinding {
        return ActivityStoreRequisitionBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rlRequisitionList.setOnClickListener {
            startActivity(Intent(this, StoreRequisitionListActivity::class.java))
        }

        binding.rlRequisitionRequest.setOnClickListener {
            startActivity(Intent(this, StoreRequisitionRequestActivity::class.java))
        }

        binding.rlTransferShipToReceive.setOnClickListener {
            startActivity(Intent(this, TransferShipToReceiveActivity::class.java))
        }
    }
}