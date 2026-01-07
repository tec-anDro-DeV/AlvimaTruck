package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.RequisitionListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityStoreRequisitionListBinding
import com.alvimatruck.utils.Utils

class StoreRequisitionListActivity : BaseActivity<ActivityStoreRequisitionListBinding>() {
    private var requisitionListAdapter: RequisitionListAdapter? = null


    override fun inflateBinding(): ActivityStoreRequisitionListBinding {
        return ActivityStoreRequisitionListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rvRequisitionList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
        binding.rvRequisitionList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        requisitionListAdapter = RequisitionListAdapter(
            this@StoreRequisitionListActivity, Utils.getDummyTransferList(5)
        ) { allSelected ->
            // This code runs when a single item in the list is clicked.
            // We check if the UI needs updating to avoid infinite loops.
            if (binding.chkAll.isChecked != allSelected) {
                binding.chkAll.isChecked = allSelected
            }
        }
        binding.rvRequisitionList.adapter = requisitionListAdapter

        binding.chkAll.setOnClickListener {
            val isChecked = binding.chkAll.isChecked
            requisitionListAdapter?.selectAll(isChecked)
        }

    }
}