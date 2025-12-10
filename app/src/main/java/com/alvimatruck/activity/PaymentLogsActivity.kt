package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.PaymentLogsListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityPaymentLogsBinding
import com.alvimatruck.utils.Utils

class PaymentLogsActivity : BaseActivity<ActivityPaymentLogsBinding>() {

    private var paymentLogsListAdapter: PaymentLogsListAdapter? = null


    override fun inflateBinding(): ActivityPaymentLogsBinding {
        return ActivityPaymentLogsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rvLogs.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
        binding.rvLogs.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        paymentLogsListAdapter = PaymentLogsListAdapter(
            this@PaymentLogsActivity, Utils.getDummyArrayList(5)
        )
        binding.rvLogs.adapter = paymentLogsListAdapter

    }
}