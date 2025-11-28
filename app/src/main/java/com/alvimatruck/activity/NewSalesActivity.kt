package com.alvimatruck.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.adapter.SingleItemSelectionAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityNewSalesBinding
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.Utils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewSalesActivity : BaseActivity<ActivityNewSalesBinding>() {
    var locationCodeList: ArrayList<String>? = ArrayList()
    var paymentCodeList: ArrayList<String>? = ArrayList()
    var itemList: ArrayList<String>? = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()
    var selectedLocationCode = ""
    var selectedPaymentCode = ""
    var selectedItem = ""


    override fun inflateBinding(): ActivityNewSalesBinding {
        return ActivityNewSalesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.tvPostingDate.text = Utils.getFullDateWithTime(System.currentTimeMillis())
        binding.tvToken.text = System.currentTimeMillis().toString()

        getLocationCodeList()
        getPaymentCodeList()
        getItemList()

        binding.tvLocationCode.setOnClickListener {
            dialogSingleSelection(
                locationCodeList!!,
                "Choose Location Code",
                "Search Location Code",
                binding.tvLocationCode
            )
        }

        binding.tvPaymentCode.setOnClickListener {
            dialogSingleSelection(
                paymentCodeList!!,
                "Choose Payment Code",
                "Search Payment Code",
                binding.tvPaymentCode
            )
        }

        binding.tvItem.setOnClickListener {
            dialogSingleSelection(
                itemList!!,
                "Choose Item",
                "Search Item",
                binding.tvPaymentCode
            )
        }


    }

    private fun dialogSingleSelection(
        list: ArrayList<String>,
        title: String,
        hint: String,
        textView: TextView
    ) {
        filterList!!.clear()
        filterList!!.addAll(list)
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_single_selection, null)
        val selectedGroup: String = when (textView) {
            binding.tvLocationCode -> {
                selectedLocationCode
            }

            binding.tvPaymentCode -> {
                selectedItem
            }

            else -> {
                selectedPaymentCode
            }
        }
        val singleItemSelectionAdapter =
            SingleItemSelectionAdapter(this, filterList!!, selectedGroup)

        val lLayout = LinearLayoutManager(this)
        val rvBinList = alertLayout.findViewById<RecyclerView>(R.id.rvItemList)
        rvBinList.layoutManager = lLayout
        rvBinList.adapter = singleItemSelectionAdapter
        val etBinSearch = alertLayout.findViewById<EditText>(R.id.etItemSearch)
        etBinSearch.hint = hint



        etBinSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                //filter(s.toString())
                filterList!!.clear()
                if (s.toString().trim().isEmpty()) {
                    filterList!!.addAll(list)
                } else {
                    for (item in list) {
                        if (item.lowercase().contains(s.toString().lowercase())) {
                            filterList!!.add(item)
                        }
                    }
                }
                singleItemSelectionAdapter.notifyDataSetChanged()
            }
        })

        val tvCancel = alertLayout.findViewById<TextView>(R.id.tvCancel2)
        val tvConfirm = alertLayout.findViewById<TextView>(R.id.tvConfirm2)
        val tvTitle = alertLayout.findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = title


        val alert = AlertDialog.Builder(this)
        alert.setView(alertLayout)
        alert.setCancelable(false)

        val dialog = alert.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        dialog.show()

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        tvCancel.setOnClickListener { view: View? -> dialog.dismiss() }
        tvConfirm.setOnClickListener { view: View? ->
            when (textView) {
                binding.tvLocationCode -> {
                    selectedLocationCode = singleItemSelectionAdapter.selected
                }

                binding.tvItem -> {
                    selectedItem = singleItemSelectionAdapter.selected
                }

                else -> {
                    selectedPaymentCode = singleItemSelectionAdapter.selected
                }
            }
            textView.text = singleItemSelectionAdapter.selected
            dialog.dismiss()
        }
    }

    private fun getLocationCodeList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@NewSalesActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.locationCodeList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            if (response.body() != null && response.body()!!.has("data")) {
                                val dataArray = response.body()!!.getAsJsonArray("data")

                                for (item in dataArray) {
                                    val obj = item.asJsonObject
                                    val code = obj.get("code").asString
                                    locationCodeList!!.add(code)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@NewSalesActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@NewSalesActivity,
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

    private fun getPaymentCodeList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@NewSalesActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.paymentCodeList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            if (response.body() != null && response.body()!!.has("data")) {
                                val dataArray = response.body()!!.getAsJsonArray("data")

                                for (item in dataArray) {
                                    val obj = item.asJsonObject
                                    val code = obj.get("code").asString
                                    paymentCodeList!!.add(code)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@NewSalesActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@NewSalesActivity,
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

    private fun getItemList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@NewSalesActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.itemList().enqueue(object : Callback<JsonArray> {
                override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse Item: " + response.body().toString())
                            if (response.body() != null) {
                                val dataArray = response.body()

                                for (item in dataArray!!) {
                                    val obj = item.asJsonObject
                                    val desc = obj.get("description")?.asString ?: ""
                                    itemList?.add(desc)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@NewSalesActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonArray?>, t: Throwable) {
                    Toast.makeText(
                        this@NewSalesActivity,
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