package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.SalesOrderListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivitySalesOrderListBinding
import com.alvimatruck.interfaces.SalesOrderClickListener
import com.alvimatruck.model.responses.OrderDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SalesOrderListActivity : BaseActivity<ActivitySalesOrderListBinding>(),
    SalesOrderClickListener {
    private var salesOrderListAdapter: SalesOrderListAdapter? = null

    var orderList: ArrayList<OrderDetail>? = ArrayList()

    var filterList: ArrayList<OrderDetail>? = ArrayList()

    override fun inflateBinding(): ActivitySalesOrderListBinding {
        return ActivitySalesOrderListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }


        binding.rvOrderList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )


        salesOrderListAPI()


//        binding.ivAddOrder.setOnClickListener {
//            startActivity(Intent(this@SalesOrderListActivity, NewSalesActivity::class.java))
//        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {

                filterList!!.clear()
                if (s.toString().trim().isEmpty()) {
                    filterList!!.addAll(orderList!!)
                } else {
                    for (item in orderList!!) {
                        if (item.customerName.lowercase()
                                .contains(s.toString().lowercase()) || item.id().lowercase()
                                .contains(s.toString().lowercase())
                        ) {
                            filterList!!.add(item)
                        }
                    }
                }
                salesOrderListAdapter!!.notifyDataSetChanged()
            }
        })


    }

    private fun salesOrderListAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@SalesOrderListActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.getSalesOrder().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@SalesOrderListActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())

                            orderList = response.body()!!.getAsJsonArray("data").map {
                                Gson().fromJson(it, OrderDetail::class.java)
                            } as ArrayList<OrderDetail>
                            filterList = ArrayList(orderList!!)
                            if (filterList!!.isNotEmpty()) {

                                binding.rvOrderList.layoutManager = LinearLayoutManager(
                                    this@SalesOrderListActivity, LinearLayoutManager.VERTICAL, false
                                )


                                salesOrderListAdapter = SalesOrderListAdapter(
                                    this@SalesOrderListActivity,
                                    filterList!!,
                                    this@SalesOrderListActivity
                                )
                                binding.rvOrderList.adapter = salesOrderListAdapter
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
                            this@SalesOrderListActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@SalesOrderListActivity,
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

    override fun onOrderClick(orderDetail: OrderDetail) {
        val intent = Intent(this, SalesOrderDetailActivity::class.java).putExtra(
            Constants.OrderID, orderDetail.orderId
        )
        startForResult.launch(intent)
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            salesOrderListAPI()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // An item was deleted, so refresh the list.
        salesOrderListAPI()
    }

}