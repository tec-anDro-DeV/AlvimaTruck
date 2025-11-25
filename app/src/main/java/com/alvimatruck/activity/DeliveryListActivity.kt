package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.DeliveryListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityDeliveryListBinding
import com.alvimatruck.utils.Utils

class DeliveryListActivity : BaseActivity<ActivityDeliveryListBinding>() {

    private var deliveryListAdapter: DeliveryListAdapter? = null

    override fun inflateBinding(): ActivityDeliveryListBinding {
        return ActivityDeliveryListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rvDeliveryList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
        binding.rvDeliveryList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        deliveryListAdapter = DeliveryListAdapter(
            this@DeliveryListActivity, Utils.getDummyArrayList(5)
        )
        binding.rvDeliveryList.adapter = deliveryListAdapter


    }
}