package com.alvimatruck.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.RouteListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityTripRouteListBinding
import com.alvimatruck.model.responses.RouteDetail
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

class TripRouteListActivity : BaseActivity<ActivityTripRouteListBinding>() {
    private var routeListAdapter: RouteListAdapter? = null

    var routeList: ArrayList<RouteDetail>? = ArrayList()


    override fun inflateBinding(): ActivityTripRouteListBinding {
        return ActivityTripRouteListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.rvRouteList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )


        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        routesListAPI()


    }

    private fun routesListAPI() {

        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@TripRouteListActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.getTodayRoutes().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@TripRouteListActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())

                            routeList = response.body()!!.getAsJsonArray("routes").map {
                                Gson().fromJson(it, RouteDetail::class.java)
                            } as ArrayList<RouteDetail>

                            if (routeList!!.isNotEmpty()) {

                                binding.rvRouteList.layoutManager =
                                    LinearLayoutManager(
                                        this@TripRouteListActivity,
                                        LinearLayoutManager.VERTICAL,
                                        false
                                    )


                                routeListAdapter = RouteListAdapter(
                                    this@TripRouteListActivity, routeList!!
                                )
                                binding.rvRouteList.adapter = routeListAdapter
                            } else {

                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@TripRouteListActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@TripRouteListActivity,
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