package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.TransferShipAndReceiveListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityTransferShipAndReciveBinding
import com.alvimatruck.utils.Utils

class TransferShipAndReceiveActivity : BaseActivity<ActivityTransferShipAndReciveBinding>() {
    private var transferShipAndReciveAdapter: TransferShipAndReceiveListAdapter? = null

    override fun inflateBinding(): ActivityTransferShipAndReciveBinding {
        return ActivityTransferShipAndReciveBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rvTransferList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
        binding.rvTransferList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        transferShipAndReciveAdapter = TransferShipAndReceiveListAdapter(
            this@TransferShipAndReceiveActivity,
            Utils.getDummyArrayList(5)
        )
        binding.rvTransferList.adapter = transferShipAndReciveAdapter

    }
}