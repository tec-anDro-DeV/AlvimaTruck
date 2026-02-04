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
import com.alvimatruck.model.responses.DriverDashboardDetail
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DriverHomeActivity : BaseActivity<ActivityDriverHomeBinding>() {
    var userDetail: UserDetail? = null

    override fun inflateBinding(): ActivityDriverHomeBinding {
        return ActivityDriverHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

//        binding.rlFeetManagement.setOnClickListener {
//            startActivity(Intent(this, FleetManagementActivity::class.java))
//        }
    }

    override fun onResume() {
        super.onResume()
        driverDashboardAPI()
    }

    private fun driverDashboardAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@DriverHomeActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.getDriverDashboardReport().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@DriverHomeActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            val dashboardDetails = Gson().fromJson(
                                response.body()!!.asJsonObject.get("data"),
                                DriverDashboardDetail::class.java
                            )
//                            binding.tvAssignOrder.text=dashboardDetails.pendingOrders.toString()
//                            binding.tvPendingOrder.text=dashboardDetails.pendingOrders.toString()
//                            binding.tvDeliveredOrder.text=dashboardDetails.pendingOrders.toString()


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
                this, getString(R.string.please_check_your_internet_connection), Toast.LENGTH_SHORT
            ).show()
        }
    }
}