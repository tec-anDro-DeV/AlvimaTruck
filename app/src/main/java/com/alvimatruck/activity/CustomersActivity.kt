package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.CustomerListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityCustomersBinding
import com.alvimatruck.model.responses.CustomerDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomersActivity : BaseActivity<ActivityCustomersBinding>() {
    private var customerListAdapter: CustomerListAdapter? = null
    var page: Int = 1
    var pageSize: Int = 50
    var customerList: ArrayList<CustomerDetail>? = ArrayList()


    override fun inflateBinding(): ActivityCustomersBinding {
        return ActivityCustomersBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rvCustomerList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )

        customerListAPI()


        binding.ivAddCustomer.setOnClickListener {
            startActivity(Intent(this@CustomersActivity, CreateCustomerActivity::class.java))
        }
    }

    private fun customerListAPI() {

        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@CustomersActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.customerList(page, pageSize).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@CustomersActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())

                            customerList = response.body()!!.getAsJsonArray("items").map {
                                Gson().fromJson(it, CustomerDetail::class.java)
                            } as ArrayList<CustomerDetail>

                            if (customerList!!.isNotEmpty()) {
                                binding.rvCustomerList.layoutManager =
                                    LinearLayoutManager(
                                        this@CustomersActivity,
                                        LinearLayoutManager.VERTICAL,
                                        false
                                    )


                                customerListAdapter = CustomerListAdapter(
                                    this@CustomersActivity, customerList!!
                                )
                                binding.rvCustomerList.adapter = customerListAdapter

                            } else {

                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@CustomersActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@CustomersActivity,
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