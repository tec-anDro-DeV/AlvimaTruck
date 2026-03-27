package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.CreditPaymentLogsListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityCreditPaymentLogsBinding
import com.alvimatruck.model.responses.CreditPaymentDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intuit.sdp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreditPaymentLogsActivity : BaseActivity<ActivityCreditPaymentLogsBinding>() {

    private var paymentLogsListAdapter: CreditPaymentLogsListAdapter? = null

    var logList: ArrayList<CreditPaymentDetail>? = ArrayList()


    override fun inflateBinding(): ActivityCreditPaymentLogsBinding {
        return ActivityCreditPaymentLogsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }

        binding.rvLogs.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )


        paymentLogsAPI()

    }

    private fun paymentLogsAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@CreditPaymentLogsActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.getCreditPaymentLogs().enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>, response: Response<JsonObject>
                ) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401 || response.code() == 402) {
                        Utils.forceLogout(
                            this@CreditPaymentLogsActivity,
                            response.code()
                        )  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())

                            logList = response.body()!!.getAsJsonArray("data").map {
                                Gson().fromJson(it, CreditPaymentDetail::class.java)
                            } as ArrayList<CreditPaymentDetail>
                            //  logList!!.sortBy { it.paymentId }
                            //   logList!!.reversed()
                            if (logList!!.isNotEmpty()) {
                                binding.rvLogs.layoutManager = LinearLayoutManager(
                                    this@CreditPaymentLogsActivity,
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )

                                paymentLogsListAdapter = CreditPaymentLogsListAdapter(
                                    this@CreditPaymentLogsActivity, logList!!
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
                            this@CreditPaymentLogsActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@CreditPaymentLogsActivity,
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