package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.TransferListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityTransferOrderListBinding
import com.alvimatruck.utils.Utils

class TransferOrderListActivity : BaseActivity<ActivityTransferOrderListBinding>() {
    private var transferListAdapter: TransferListAdapter? = null

    override fun inflateBinding(): ActivityTransferOrderListBinding {
        return ActivityTransferOrderListBinding.inflate(layoutInflater)
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


        transferListAdapter = TransferListAdapter(
            this@TransferOrderListActivity, Utils.getDummyArrayList(5)
        )
        binding.rvTransferList.adapter = transferListAdapter

    }
}