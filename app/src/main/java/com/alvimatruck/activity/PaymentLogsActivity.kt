package com.alvimatruck.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.PaymentLogsListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityPaymentLogsBinding
import com.alvimatruck.model.responses.PaymentDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentLogsActivity : BaseActivity<ActivityPaymentLogsBinding>() {

    private var paymentLogsListAdapter: PaymentLogsListAdapter? = null

    var logList: ArrayList<PaymentDetail>? = ArrayList()


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


        paymentLogsAPI()

    }

    private fun paymentLogsAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@PaymentLogsActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.getPaymentLogs()
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>, response: Response<JsonObject>
                    ) {
                        ProgressDialog.dismiss()
                        if (response.code() == 401) {
                            Utils.forceLogout(this@PaymentLogsActivity)  // show dialog before logout
                            return
                        }
                        if (response.isSuccessful) {
                            try {
                                Log.d("TAG", "onResponse: " + response.body().toString())

                                logList = response.body()!!.getAsJsonArray("items").map {
                                    Gson().fromJson(it, PaymentDetail::class.java)
                                } as ArrayList<PaymentDetail>
                                if (logList!!.isNotEmpty()) {
                                    binding.rvLogs.layoutManager =
                                        LinearLayoutManager(
                                            this@PaymentLogsActivity,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )

                                    paymentLogsListAdapter = PaymentLogsListAdapter(
                                        this@PaymentLogsActivity, logList!!
                                    )
                                    binding.rvLogs.adapter = paymentLogsListAdapter

                                    binding.llNoData.root.visibility = View.GONE
                                    binding.rvLogs.visibility = View.VISIBLE
                                } else {
                                    binding.llNoData.root.visibility = View.VISIBLE
                                    binding.rvLogs.visibility = View.GONE
                                }


                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                this@PaymentLogsActivity,
                                Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@PaymentLogsActivity,
                            getString(com.alvimatruck.R.string.api_fail_message),
                            Toast.LENGTH_SHORT
                        ).show()
                        ProgressDialog.dismiss()
                    }
                })
        } else {
            Toast.makeText(
                this,
                getString(com.alvimatruck.R.string.please_check_your_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
        }

    }
}