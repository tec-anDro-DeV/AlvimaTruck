package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.alvimatruck.R
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityFleetManagementBinding
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

class FleetManagementActivity : BaseActivity<ActivityFleetManagementBinding>() {
    var logList: ArrayList<FleetLog>? = ArrayList()


    override fun inflateBinding(): ActivityFleetManagementBinding {
        return ActivityFleetManagementBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rlFuelRefillRequest.setOnClickListener {
            val intent = Intent(this, FuelRefillRequestActivity::class.java)
            startForResult.launch(intent)
        }

        binding.rlRepairLog.setOnClickListener {
            val intent = Intent(this, RepairLogActivity::class.java)
            startForResult.launch(intent)
        }

        binding.rlIncidentReporting.setOnClickListener {
            val intent = Intent(this, IncidentReportingActivity::class.java)
            startForResult.launch(intent)
        }

        binding.tvSeeAll.setOnClickListener {
            startActivity(Intent(this, RecentLogsActivity::class.java))
        }

        getLogs()

    }

    private fun getLogs() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@FleetManagementActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.fleetLogList(page = 1, pageSize = 1)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>, response: Response<JsonObject>
                    ) {
                        ProgressDialog.dismiss()
                        if (response.code() == 401) {
                            Utils.forceLogout(this@FleetManagementActivity)  // show dialog before logout
                            return
                        }
                        if (response.isSuccessful) {
                            try {
                                Log.d("TAG", "onResponse: " + response.body().toString())

                                logList = response.body()!!.getAsJsonArray("items").map {
                                    Gson().fromJson(it, FleetLog::class.java)
                                } as ArrayList<FleetLog>
                                if (logList!!.isNotEmpty()) {
                                    binding.llLogs.visibility = View.VISIBLE
                                    binding.tvOrderId.text = "Req No. - " + logList!![0].uniqueId
                                    binding.tvStatus.text = logList!![0].status
                                    binding.tvFleetType.text = logList!![0].fleetType
                                    binding.tvVendorDetails.text =
                                        logList!![0].repairLogVendorDetail
                                    binding.tvDescription.text =
                                        logList!![0].incidentReportDescription
                                    binding.tvRequestDate.text = logList!![0].getRequestDate()
                                    binding.tvCost.text = logList!![0].getCost()
                                    binding.tvIncidentType.text = logList!![0].incidentReportType

                                    when (logList!![0].fleetType) {
                                        "FuleRefill" -> {
                                            binding.rlVendor.visibility = View.GONE
                                            binding.rlDescription.visibility = View.GONE
                                            binding.rlCost.visibility = View.VISIBLE
                                            binding.llType.visibility = View.GONE
                                        }

                                        "RepairLog" -> {
                                            binding.rlVendor.visibility = View.VISIBLE
                                            binding.rlDescription.visibility = View.GONE
                                            binding.rlCost.visibility = View.VISIBLE
                                            binding.llType.visibility = View.GONE
                                        }

                                        else -> {
                                            binding.rlVendor.visibility = View.GONE
                                            binding.rlDescription.visibility = View.VISIBLE
                                            binding.rlCost.visibility = View.GONE
                                            binding.llType.visibility = View.VISIBLE
                                        }
                                    }

                                    if (logList!![0].status == "Approved") {
                                        binding.tvStatus.background =
                                            ContextCompat.getDrawable(
                                                this@FleetManagementActivity,
                                                R.drawable.bg_status_green
                                            )
                                    } else {
                                        binding.tvStatus.background =
                                            ContextCompat.getDrawable(
                                                this@FleetManagementActivity,
                                                R.drawable.bg_status_red
                                            )
                                    }

                                } else {
                                    binding.llLogs.visibility = View.GONE

                                }


                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                this@FleetManagementActivity,
                                Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@FleetManagementActivity,
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


    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            getLogs()
        }
    }
}