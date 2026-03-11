package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.alvimatruck.R
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityNewSendPaymentBinding
import com.alvimatruck.model.responses.InvoiceDetail
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

class NewSendPaymentActivity : BaseActivity<ActivityNewSendPaymentBinding>() {
    var invoiceList: ArrayList<InvoiceDetail>? = ArrayList()
    var selectedInvoiceList: ArrayList<String>? = ArrayList()
    var total = 0.0
    var userDetail: UserDetail? = null

    override fun inflateBinding(): ActivityNewSendPaymentBinding {
        return ActivityNewSendPaymentBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userDetail =
            Gson().fromJson(SharedHelper.getKey(this, Constants.UserDetail), UserDetail::class.java)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }

        binding.tvBatchName.text = userDetail?.salesPersonCode
        invoiceListAPI()
    }

    private fun invoiceListAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@NewSendPaymentActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.invoiceList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>, response: Response<JsonObject>
                ) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@NewSendPaymentActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            invoiceList = response.body()!!.getAsJsonArray("data").map {
                                Gson().fromJson(it, InvoiceDetail::class.java)
                            } as ArrayList<InvoiceDetail>

                            if (invoiceList!!.isNotEmpty()) {
                                binding.llInvoice.visibility = View.VISIBLE
                                setupInvoiceCheckboxes()
                            } else {
                                binding.llInvoice.visibility = View.GONE
                                Toast.makeText(
                                    this@NewSendPaymentActivity,
                                    "No invoice found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@NewSendPaymentActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@NewSendPaymentActivity,
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

    private fun setupInvoiceCheckboxes() {
        binding.llInvoice.removeAllViews()

        invoiceList?.forEachIndexed { index, invoice ->

            val view = layoutInflater.inflate(R.layout.single_invoice, binding.llInvoice, false)

            val cb = view.findViewById<CheckBox>(R.id.cbInvoice)
            val root = view.findViewById<LinearLayout>(R.id.rootRow)
            val tvNo = view.findViewById<TextView>(R.id.tvInvoiceNo)
            //val tvDate = view.findViewById<TextView>(R.id.tvInvoiceDate)
            val tvAmount = view.findViewById<TextView>(R.id.tvInvoiceAmount)
            val divider = view.findViewById<View>(R.id.viewDivider)

            tvNo.text = invoice.documentNo
            //   tvDate.text = "Date: ${invoice.getRequestDate() ?: "-"}"
            tvAmount.text = "ETB ${invoice.remainingAmount}"

            cb.tag = invoice
            cb.setOnCheckedChangeListener { _, _ ->
                updateTotal()
            }

            if (invoiceList!!.size - 1 == index) {
                divider.visibility = View.GONE
            } else {
                divider.visibility = View.VISIBLE
            }

            root.setOnClickListener {
                cb.isChecked = !cb.isChecked
            }

            binding.llInvoice.addView(view)
        }
    }

    private fun updateTotal() {
        selectedInvoiceList!!.clear()
        total = 0.0

        for (i in 0 until binding.llInvoice.childCount) {
            val row = binding.llInvoice.getChildAt(i)

            val checkBox = row.findViewById<CheckBox>(R.id.cbInvoice)

            if (checkBox.isChecked) {
                val invoice = checkBox.tag as InvoiceDetail
                selectedInvoiceList!!.add(invoice.documentNo)
                total += invoice.remainingAmount
            }
        }

        binding.tvtotal.text = "ETB $total"
    }

}