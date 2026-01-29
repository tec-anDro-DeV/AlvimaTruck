package com.alvimatruck.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.DeliveryListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityDeliveryListBinding
import com.alvimatruck.model.responses.DeliveryTripDetail
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DeliveryListActivity : BaseActivity<ActivityDeliveryListBinding>() {
    var userDetail: UserDetail? = null
    private val todayDate: Calendar = Calendar.getInstance()
    private val todayDateStr: String
        get() = dateFormatterAPI.format(todayDate.time)
    private var deliveryListAdapter: DeliveryListAdapter? = null

    private val dateFormatterAPI = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var orderList: ArrayList<DeliveryTripDetail>? = ArrayList()

    var filterList: ArrayList<DeliveryTripDetail>? = ArrayList()


    override fun inflateBinding(): ActivityDeliveryListBinding {
        return ActivityDeliveryListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userDetail =
            Gson().fromJson(SharedHelper.getKey(this, Constants.UserDetail), UserDetail::class.java)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rvDeliveryList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )




        getDriverTrip()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {

                filterList!!.clear()
                if (s.toString().trim().isEmpty()) {
                    filterList!!.addAll(orderList!!)
                } else {
                    for (item in orderList!!) {
                        if (item.sellToCustomerName.lowercase()
                                .contains(s.toString().lowercase()) || item.no.lowercase()
                                .contains(s.toString().lowercase())
                        ) {
                            filterList!!.add(item)
                        }
                    }
                }
                deliveryListAdapter!!.notifyDataSetChanged()
            }
        })


    }

    private fun getDriverTrip() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@DeliveryListActivity)
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
                            Utils.forceLogout(this@DeliveryListActivity)  // show dialog before logout
                            return
                        }
                        if (response.isSuccessful) {
                            try {
                                Log.d("TAG", "onResponse: " + response.body().toString())
                                orderList = response.body()!!.getAsJsonArray("data").map {
                                    Gson().fromJson(it, DeliveryTripDetail::class.java)
                                } as ArrayList<DeliveryTripDetail>
                                filterList = ArrayList(orderList!!)
                                if (filterList!!.isNotEmpty()) {

                                    binding.rvDeliveryList.layoutManager =
                                        LinearLayoutManager(
                                            this@DeliveryListActivity,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )

                                    deliveryListAdapter = DeliveryListAdapter(
                                        this@DeliveryListActivity, filterList!!
                                    )
                                    binding.rvDeliveryList.adapter = deliveryListAdapter
                                    binding.llData.visibility = View.VISIBLE
                                    binding.llNoData.root.visibility = View.GONE

                                } else {
                                    binding.llData.visibility = View.GONE
                                    binding.llNoData.root.visibility = View.VISIBLE
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                this@DeliveryListActivity,
                                Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@DeliveryListActivity,
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