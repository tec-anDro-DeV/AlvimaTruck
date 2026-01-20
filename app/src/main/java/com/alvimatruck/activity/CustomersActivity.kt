package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.CustomerListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityCustomersBinding
import com.alvimatruck.interfaces.CustomerClickListener
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

class CustomersActivity : BaseActivity<ActivityCustomersBinding>(), CustomerClickListener {
    private var customerListAdapter: CustomerListAdapter? = null

    var routeName = ""
    var customerList: ArrayList<CustomerDetail>? = ArrayList()
    var filterList: ArrayList<CustomerDetail>? = ArrayList()


    private val openUpdateCustomer =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedCustomer = Gson().fromJson(
                    result.data?.getStringExtra(Constants.CustomerDetail).toString(),
                    CustomerDetail::class.java
                )

                if (updatedCustomer != null) {
                    val index =
                        filterList!!.indexOfFirst { it.no == updatedCustomer.no }  // match customerId
                    if (index != -1) {
                        filterList!![index] = updatedCustomer
                        customerListAdapter!!.notifyItemChanged(index)
                    }
                }
            }
        }


    override fun inflateBinding(): ActivityCustomersBinding {
        return ActivityCustomersBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        if (intent != null) {
            routeName = intent.getStringExtra(Constants.RouteDetail).toString()
        }


        binding.rvCustomerList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )

        customerListAPI()


        binding.ivAddCustomer.setOnClickListener {
            // startActivity(Intent(this@CustomersActivity, CreateCustomerActivity::class.java))
            val intent = Intent(this, CreateCustomerActivity::class.java)
            startForResult.launch(intent)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {

                filterList!!.clear()
                if (s.toString().trim().isEmpty()) {
                    filterList!!.addAll(customerList!!)
                } else {
                    for (item in customerList!!) {
                        if (item.searchName.lowercase().contains(s.toString().lowercase())) {
                            filterList!!.add(item)
                        }
                    }
                }
                customerListAdapter!!.notifyDataSetChanged()
            }
        })
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // NEW CUSTOMER ADDED → Refresh list
            customerListAPI()
        }
    }

    private fun customerListAPI() {

        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@CustomersActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.customerList(routeName = routeName)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>, response: Response<JsonObject>
                    ) {

                        if (response.code() == 401) {
                            ProgressDialog.dismiss()
                            Utils.forceLogout(this@CustomersActivity)  // show dialog before logout
                            return
                        }
                        if (response.isSuccessful) {
                            try {
                                Log.d("TAG", "onResponse: " + response.body().toString())

                                customerList = response.body()!!.getAsJsonArray("items").map {
                                    Gson().fromJson(it, CustomerDetail::class.java)
                                } as ArrayList<CustomerDetail>
                                filterList = ArrayList(customerList!!)
                                if (filterList!!.isNotEmpty()) {
                                    binding.rvCustomerList.layoutManager = LinearLayoutManager(
                                        this@CustomersActivity, LinearLayoutManager.VERTICAL, false
                                    )


                                    customerListAdapter = CustomerListAdapter(
                                        this@CustomersActivity, filterList!!, this@CustomersActivity
                                    )
                                    binding.rvCustomerList.adapter = customerListAdapter
                                    binding.llData.visibility = View.VISIBLE
                                    binding.llNoData.root.visibility = View.GONE

                                    binding.rvCustomerList.post {
                                        ProgressDialog.dismiss()
                                    }

                                } else {
                                    ProgressDialog.dismiss()
                                    binding.llData.visibility = View.GONE
                                    binding.llNoData.root.visibility = View.VISIBLE
                                }


                            } catch (e: Exception) {
                                ProgressDialog.dismiss()
                                e.printStackTrace()
                            }
                        } else {
                            ProgressDialog.dismiss()
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

    override fun onCustomerClick(customerDetail: CustomerDetail) {
        val intent = Intent(this, ViewCustomerActivity::class.java).putExtra(
            Constants.CustomerDetail, Gson().toJson(customerDetail)
        )
        openUpdateCustomer.launch(intent)
    }

    override fun handleBackPressed(callback: OnBackPressedCallback?) {
        if (Utils.isNewOrder) {
            val intent = Intent(this, TripRouteListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        } else {
            super.handleBackPressed(callback)
        }
    }


}