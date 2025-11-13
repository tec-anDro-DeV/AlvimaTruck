package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.CustomerListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityCustomersBinding
import com.alvimatruck.utils.Utils

class CustomersActivity : BaseActivity<ActivityCustomersBinding>() {
    private var customerListAdapter: CustomerListAdapter? = null

    override fun inflateBinding(): ActivityCustomersBinding {
        return ActivityCustomersBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rvCustomerList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
        binding.rvCustomerList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        customerListAdapter = CustomerListAdapter(
            this@CustomersActivity, Utils.getDummyArrayList(5)
        )
        binding.rvCustomerList.adapter = customerListAdapter
    }


}