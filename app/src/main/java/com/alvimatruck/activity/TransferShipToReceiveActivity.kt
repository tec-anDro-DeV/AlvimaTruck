package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.TransferShipToReceiveListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityTransferShipToReceiveBinding
import com.alvimatruck.utils.Utils

class TransferShipToReceiveActivity : BaseActivity<ActivityTransferShipToReceiveBinding>() {
    private var transferShipToReceiveListAdapter: TransferShipToReceiveListAdapter? = null

    override fun inflateBinding(): ActivityTransferShipToReceiveBinding {
        return ActivityTransferShipToReceiveBinding.inflate(layoutInflater)
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


        transferShipToReceiveListAdapter = TransferShipToReceiveListAdapter(
            this@TransferShipToReceiveActivity, Utils.getDummyTransferList(5)
        ) { allSelected ->
            // This code runs when a single item in the list is clicked.
            // We check if the UI needs updating to avoid infinite loops.
            if (binding.chkAll.isChecked != allSelected) {
                binding.chkAll.isChecked = allSelected
            }
        }
        binding.rvTransferList.adapter = transferShipToReceiveListAdapter

        binding.chkAll.setOnClickListener {
            val isChecked = binding.chkAll.isChecked
            transferShipToReceiveListAdapter?.selectAll(isChecked)
        }


    }
}