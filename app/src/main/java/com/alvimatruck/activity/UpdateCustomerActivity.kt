package com.alvimatruck.activity

import android.content.Intent
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
import com.alvimatruck.databinding.ActivityUpdateCustomerBinding
import com.alvimatruck.model.request.CustomerUpdate
import com.alvimatruck.model.responses.CityDetail
import com.alvimatruck.model.responses.CustomerDetail
import com.alvimatruck.service.AlvimaTuckApplication
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateCustomerActivity : BaseActivity<ActivityUpdateCustomerBinding>() {
    var customerDetail: CustomerDetail? = null
    var selectedCity = ""
    var cityList: ArrayList<String>? = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()

    var postalCodeList: ArrayList<CityDetail>? = ArrayList()
    override fun inflateBinding(): ActivityUpdateCustomerBinding {
        return ActivityUpdateCustomerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        checkAndStartLocationService()
        getCityList()

        if (intent != null) {
            customerDetail = Gson().fromJson(
                intent.getStringExtra(Constants.CustomerDetail).toString(),
                CustomerDetail::class.java
            )
            binding.etCustomerName.text = customerDetail!!.searchName
            binding.etTelephoneNumber.setText(customerDetail!!.telexNo)
            binding.tvCity.text = customerDetail!!.city
            selectedCity = customerDetail!!.city!!
            binding.tvPostalCode.text = customerDetail!!.postCode
            binding.etAddress.setText(customerDetail!!.address)
        }

        binding.tvUpdate.setOnClickListener {
            validationAndSubmit()
        }

        binding.tvCancel.setOnClickListener {
            handleBackPressed()
        }

        binding.tvCity.setOnClickListener {
            dialogSingleSelection(
                cityList!!,
                "Choose City",
                "Search City",
                binding.tvCity,
                binding.tvPostalCode
            )
        }
    }

    private fun dialogSingleSelection(
        list: ArrayList<String>,
        title: String,
        hint: String,
        textView: TextView,
        textView2: TextView? = null
    ) {
        filterList!!.clear()
        filterList!!.addAll(list)
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_single_selection, null)
        val selectedGroup: String = selectedCity
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
            selectedCity = singleItemSelectionAdapter.selected

            for (item in postalCodeList!!) {
                if (item.city == singleItemSelectionAdapter.selected) {
                    textView2?.text = item.code
                }
            }
            textView.text = singleItemSelectionAdapter.selected
            dialog.dismiss()
        }
    }

    private fun getCityList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@UpdateCustomerActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.cityList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            if (response.body() != null && response.body()!!.has("data")) {
                                postalCodeList = response.body()!!.getAsJsonArray("data").map {
                                    Gson().fromJson(it, CityDetail::class.java)
                                } as ArrayList<CityDetail>
                                for (item in postalCodeList!!) {
                                    val code = item.city
                                    cityList!!.add(code)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@UpdateCustomerActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@UpdateCustomerActivity,
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


    private fun validationAndSubmit() {
        if (binding.etTelephoneNumber.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter telephone number", Toast.LENGTH_SHORT).show()
            return
        } else if (!Utils.isValidEthiopiaLocalNumber(
                binding.etTelephoneNumber.text.toString().trim()
            )
        ) {
            Toast.makeText(this, "Please enter valid telephone number", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.tvCity.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select city", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.tvPostalCode.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select postal code", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.etAddress.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter address", Toast.LENGTH_SHORT).show()
            return
        } else {
            updateCustomerApiCall()
        }
    }

    private fun updateCustomerApiCall() {
        if (Utils.isOnline(this)) {

            ProgressDialog.start(this@UpdateCustomerActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.updateCustomer(
                CustomerUpdate(
                    binding.etAddress.text.toString().trim(),

                    binding.tvCity.text.toString().trim(),
                    AlvimaTuckApplication.latitude,
                    AlvimaTuckApplication.longitude,
                    customerDetail!!.no,
                    binding.tvPostalCode.text.toString().trim(),
                    binding.etTelephoneNumber.text.toString().trim()
                )
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@UpdateCustomerActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@UpdateCustomerActivity,
                                response.body()!!.get("message").toString().replace('"', ' ')
                                    .trim(),
                                Toast.LENGTH_SHORT
                            ).show()

                            customerDetail!!.telexNo =
                                binding.etTelephoneNumber.text.toString().trim()
                            customerDetail!!.city = binding.tvCity.text.toString().trim()
                            customerDetail!!.address = binding.etAddress.text.toString().trim()
                            customerDetail!!.postCode = binding.tvPostalCode.text.toString().trim()


                            val intent = Intent()
                            intent.putExtra(Constants.CustomerDetail, Gson().toJson(customerDetail))
                            setResult(RESULT_OK, intent)
                            finish()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@UpdateCustomerActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@UpdateCustomerActivity,
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