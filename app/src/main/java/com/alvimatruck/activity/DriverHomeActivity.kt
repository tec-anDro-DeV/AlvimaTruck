package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.alvimatruck.R
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityDriverHomeBinding
import com.alvimatruck.model.responses.DeliveryTripDetail
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.alvimatruck.utils.Utils.DriverVanNo
import com.alvimatruck.utils.Utils.driverOrderList
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DriverHomeActivity : BaseActivity<ActivityDriverHomeBinding>() {
    var userDetail: UserDetail? = null

    private val todayDate: Calendar = Calendar.getInstance()
    private val dateFormatterAPI = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val todayDateStr: String
        get() = dateFormatterAPI.format(todayDate.time)


    override fun inflateBinding(): ActivityDriverHomeBinding {
        return ActivityDriverHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndStartLocationService()

        binding.tvDate.text = Utils.getFullDate(System.currentTimeMillis())

        userDetail =
            Gson().fromJson(SharedHelper.getKey(this, Constants.UserDetail), UserDetail::class.java)
        binding.tvUsername.text = userDetail?.driverFullName

        binding.rlDelivery.setOnClickListener {
            startActivity(Intent(this, DeliveryListActivity::class.java))
        }

//        binding.rlRoute.setOnClickListener {
//            startActivity(Intent(this, DeliveryTripRouteActivity::class.java))
//        }

        binding.rlReport.setOnClickListener {
            startActivity(Intent(this, DeliveryTripReportActivity::class.java))
        }


        binding.rlLogout.setOnClickListener {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_logout, null)

            val btnNo = alertLayout.findViewById<TextView>(R.id.btnNo)
            val btnYes = alertLayout.findViewById<TextView>(R.id.btnYes)

            val dialog =
                AlertDialog.Builder(this).setView(alertLayout).setCancelable(false).create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


            btnNo.setOnClickListener {
                dialog.dismiss()
            }
            btnYes.setOnClickListener {
                dialog.dismiss()
                Utils.logout(this)

            }
            dialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        binding.rlSync.setOnClickListener {
            getDriverTrip()
        }

//        binding.rlFeetManagement.setOnClickListener {
//            startActivity(Intent(this, FleetManagementActivity::class.java))
//        }

        getDriverTrip()
    }

    override fun onResume() {
        super.onResume()
        setupData()
    }

    private fun getDriverTrip() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@DriverHomeActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.driverTripList(userDetail!!.driverNo, todayDateStr)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        ProgressDialog.dismiss()
                        if (response.code() == 401) {
                            Utils.forceLogout(this@DriverHomeActivity)  // show dialog before logout
                            return
                        }
                        if (response.isSuccessful) {
                            try {
                                Log.d("TAG", "onResponse: " + response.body().toString())
                                driverOrderList = response.body()!!.getAsJsonArray("data").map {
                                    Gson().fromJson(it, DeliveryTripDetail::class.java)
                                } as ArrayList<DeliveryTripDetail>
                                setupData()


                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                this@DriverHomeActivity,
                                Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@DriverHomeActivity,
                            getString(R.string.api_fail_message),
                            Toast.LENGTH_SHORT
                        ).show()
                        ProgressDialog.dismiss()
                    }
                })
        } else {
            Toast.makeText(
                this,
                getString(R.string.please_check_your_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun setupData() {
        DriverVanNo = userDetail!!.plateNo
        if (driverOrderList!!.isNotEmpty()) {
            binding.tvAssignOrder.text = driverOrderList?.size.toString()
            val openCount =
                driverOrderList!!.count { it.appStatus == "Open" || it.appStatus == "InProgress" }
            binding.tvPendingOrder.text = openCount.toString()
            val pendingCount =
                driverOrderList!!.count { it.appStatus == "Cancelled" || it.appStatus == "Delivered" || it.appStatus == "Completed" }
            binding.tvDeliveredOrder.text = pendingCount.toString()
        } else {
            binding.tvAssignOrder.text = "0"
            binding.tvPendingOrder.text = "0"
            binding.tvDeliveredOrder.text = "0"
        }
    }
}