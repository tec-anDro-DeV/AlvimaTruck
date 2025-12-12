package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.SalesOrderListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivitySalesOrderListBinding
import com.alvimatruck.utils.Utils

class SalesOrderListActivity : BaseActivity<ActivitySalesOrderListBinding>() {
    private var salesOrderListAdapter: SalesOrderListAdapter? = null
    override fun inflateBinding(): ActivitySalesOrderListBinding {
        return ActivitySalesOrderListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }


        binding.rvOrderList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
        binding.rvOrderList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        salesOrderListAdapter = SalesOrderListAdapter(
            this@SalesOrderListActivity, Utils.getDummyArrayList(5)
        )
        binding.rvOrderList.adapter = salesOrderListAdapter


//        binding.ivAddOrder.setOnClickListener {
//            startActivity(Intent(this@SalesOrderListActivity, NewSalesActivity::class.java))
//        }

    }
}