package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.SellOrderListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivitySellOrderListBinding
import com.alvimatruck.utils.Utils

class SellOrderListActivity : BaseActivity<ActivitySellOrderListBinding>() {
    private var sellOrderListAdapter: SellOrderListAdapter? = null
    override fun inflateBinding(): ActivitySellOrderListBinding {
        return ActivitySellOrderListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.rvOrderList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
        binding.rvOrderList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        sellOrderListAdapter = SellOrderListAdapter(
            this@SellOrderListActivity, Utils.getDummyArrayList(5)
        )
        binding.rvOrderList.adapter = sellOrderListAdapter

    }
}