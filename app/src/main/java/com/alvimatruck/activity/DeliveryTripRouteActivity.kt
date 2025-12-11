package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.DeliveryRouteListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityDeliveryTripRouteBinding
import com.alvimatruck.interfaces.DeliveryRouteClickListener
import com.alvimatruck.model.responses.RouteDetail
import com.alvimatruck.utils.Utils
import com.intuit.sdp.R

class DeliveryTripRouteActivity : BaseActivity<ActivityDeliveryTripRouteBinding>(),
    DeliveryRouteClickListener {
    private var routeListAdapter: DeliveryRouteListAdapter? = null

    var routeList: ArrayList<RouteDetail>? = ArrayList()


    override fun inflateBinding(): ActivityDeliveryTripRouteBinding {
        return ActivityDeliveryTripRouteBinding.inflate(layoutInflater)
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

        binding.swipeRefresh.setOnRefreshListener {
            //  binding.swipeRefresh.isRefreshing = true
            routesListAPI()
        }

        routesListAPI()

    }

    private fun routesListAPI() {
        binding.swipeRefresh.isRefreshing = false

        binding.rvRouteList.layoutManager =
            LinearLayoutManager(
                this@DeliveryTripRouteActivity,
                LinearLayoutManager.VERTICAL,
                false
            )


        routeListAdapter = DeliveryRouteListAdapter(
            this@DeliveryTripRouteActivity,
            Utils.getDummyArrayList(5),
            this@DeliveryTripRouteActivity
        )
        binding.rvRouteList.adapter = routeListAdapter
//        if (Utils.isOnline(this)) {
//            ProgressDialog.start(this@TripRouteListActivity)
//            ApiClient.getRestClient(
//                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
//            )!!.webservices.getTodayRoutes().enqueue(object : Callback<JsonObject> {
//                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
//                    ProgressDialog.dismiss()
//                    if (response.code() == 401) {
//                        Utils.forceLogout(this@TripRouteListActivity)  // show dialog before logout
//                        return
//                    }
//                    if (response.isSuccessful) {
//                        try {
//                            Log.d("TAG", "onResponse: " + response.body().toString())
//
//                            routeList = response.body()!!.getAsJsonArray("routes").map {
//                                Gson().fromJson(it, RouteDetail::class.java)
//                            } as ArrayList<RouteDetail>
//
//                            if (routeList!!.isNotEmpty()) {
//
//                                binding.rvRouteList.layoutManager =
//                                    LinearLayoutManager(
//                                        this@TripRouteListActivity,
//                                        LinearLayoutManager.VERTICAL,
//                                        false
//                                    )
//
//
//                                routeListAdapter = RouteListAdapter(
//                                    this@TripRouteListActivity,
//                                    routeList!!,
//                                    this@TripRouteListActivity
//                                )
//                                binding.rvRouteList.adapter = routeListAdapter
//                            } else {
//
//                            }
//
//
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    } else {
//                        Toast.makeText(
//                            this@TripRouteListActivity,
//                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
//                    Toast.makeText(
//                        this@TripRouteListActivity,
//                        getString(com.alvimatruck.R.string.api_fail_message),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    ProgressDialog.dismiss()
//                }
//            })
//        } else {
//            Toast.makeText(
//                this,
//                getString(com.alvimatruck.R.string.please_check_your_internet_connection),
//                Toast.LENGTH_SHORT
//            ).show()
//        }

    }


    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            routesListAPI()
        }
    }

    override fun onRouteClick(routeDetail: String) {
//        val intent = Intent(this, RouteDetailActivity::class.java).putExtra(
//            Constants.Status, routeDetail.status
//        ).putExtra(Constants.RouteDetail, Gson().toJson(routeDetail))
//        startForResult.launch(intent)
        startActivity(Intent(this, DeliveryRouteDetailActivity::class.java))
    }
}