package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.R
import com.alvimatruck.adapter.NewSalesItemListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivitySalesOrderDetailBinding
import com.alvimatruck.model.responses.FullOrderDetail
import com.alvimatruck.model.responses.SingleOrder
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.alvimatruck.utils.Utils.to2Decimal
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SalesOrderDetailActivity : BaseActivity<ActivitySalesOrderDetailBinding>() {
    var newSalesItemListAdapter: NewSalesItemListAdapter? = null

    var orderList: ArrayList<SingleOrder> = ArrayList()

    var orderID: String? = null
    var orderDetail: FullOrderDetail? = null
    override fun inflateBinding(): ActivitySalesOrderDetailBinding {
        return ActivitySalesOrderDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) {
            orderID = intent.getStringExtra(Constants.OrderID).toString()
            getSalesOrderDetailAPI()
        }


        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.btnEdit.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    EditSalesActivity::class.java
                ).putExtra(Constants.OrderDetail, Gson().toJson(orderDetail))
            )
        }
        binding.rvProducts.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        newSalesItemListAdapter = NewSalesItemListAdapter(
            this@SalesOrderDetailActivity, orderList, null
        )
        binding.rvProducts.adapter = newSalesItemListAdapter


    }

    private fun getSalesOrderDetailAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@SalesOrderDetailActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.orderDetail(
                orderID.toString()
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@SalesOrderDetailActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            orderDetail = response.body()!!.getAsJsonObject("data").let {
                                Gson().fromJson(it, FullOrderDetail::class.java)
                            }
                            binding.tvOrderId.text = orderDetail!!.orderId
                            binding.tvInvoice.text =
                                orderDetail!!.invoiceNo.takeIf { it.isNullOrEmpty() } ?: "-"
                            binding.tvCustomerName.text = orderDetail!!.customerName
                            binding.tvContactNumber.text = orderDetail!!.getFormattedContactNo()
                            binding.tvAddress.text = orderDetail!!.getFullAddress()
                            orderList.clear()
                            orderList.addAll(orderDetail!!.lines)
                            newSalesItemListAdapter!!.notifyDataSetChanged()
                            binding.tvDeliveryDate.text =
                                "Delivered on " + orderDetail!!.getRequestDate()
                            binding.tvOrderDate.text = orderDetail!!.getRequestDate()
                            binding.tvSubTotal.text = "ETB " + orderDetail!!.subtotal.to2Decimal()
                            binding.tvVat.text = "+ " + "ETB " + orderDetail!!.vat.to2Decimal()
                            binding.tvTotal.text = "ETB " + orderDetail!!.total.to2Decimal()

                            if (orderDetail!!.invoiceNo == null || orderDetail!!.invoiceNo == "") {
                                binding.tvStatus.text = "Open"
                                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_red)
                            } else {
                                binding.tvStatus.text = "Delivered"
                                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_green)
                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@SalesOrderDetailActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@SalesOrderDetailActivity,
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