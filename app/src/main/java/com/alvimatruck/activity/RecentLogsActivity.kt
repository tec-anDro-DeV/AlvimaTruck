package com.alvimatruck.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.RecentLogsListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityRecentLogsBinding
import com.alvimatruck.model.responses.FleetLog
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecentLogsActivity : BaseActivity<ActivityRecentLogsBinding>() {
    private var recentLogsListAdapter: RecentLogsListAdapter? = null

    var logList: ArrayList<FleetLog>? = ArrayList()

    override fun inflateBinding(): ActivityRecentLogsBinding {
        return ActivityRecentLogsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        getLogs()

        binding.rvLogs.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
    }


    private fun getLogs() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@RecentLogsActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.fleetLogList(page = 1, pageSize = 100)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>, response: Response<JsonObject>
                    ) {
                        ProgressDialog.dismiss()
                        if (response.code() == 401) {
                            Utils.forceLogout(this@RecentLogsActivity)  // show dialog before logout
                            return
                        }
                        if (response.isSuccessful) {
                            try {
                                Log.d("TAG", "onResponse: " + response.body().toString())

                                logList = response.body()!!.getAsJsonArray("items").map {
                                    Gson().fromJson(it, FleetLog::class.java)
                                } as ArrayList<FleetLog>
                                if (logList!!.isNotEmpty()) {
                                    binding.rvLogs.layoutManager = LinearLayoutManager(
                                        this@RecentLogsActivity, LinearLayoutManager.VERTICAL, false
                                    )


                                    recentLogsListAdapter = RecentLogsListAdapter(
                                        this@RecentLogsActivity, logList!!
                                    )
                                    binding.rvLogs.adapter = recentLogsListAdapter

                                } else {

                                }


                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                this@RecentLogsActivity,
                                Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@RecentLogsActivity,
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