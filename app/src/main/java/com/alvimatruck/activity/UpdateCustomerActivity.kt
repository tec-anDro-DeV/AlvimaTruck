package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.alvimatruck.R
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityUpdateCustomerBinding
import com.alvimatruck.model.request.CustomerUpdate
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
    override fun inflateBinding(): ActivityUpdateCustomerBinding {
        return ActivityUpdateCustomerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        checkAndStartLocationService()

        if (intent != null) {
            customerDetail = Gson().fromJson(
                intent.getStringExtra(Constants.CustomerDetail).toString(),
                CustomerDetail::class.java
            )
            binding.etCustomerName.text = customerDetail!!.searchName
            binding.etTelephoneNumber.setText(customerDetail!!.telexNo)
            binding.etCity.setText(customerDetail!!.city)
            // binding.etPostalCode.setText(customerDetail!!.)
            binding.etAddress.setText(customerDetail!!.address + " " + customerDetail!!.address2)
        }

        binding.tvUpdate.setOnClickListener {
            validationAndSubmit()
        }

        binding.tvCancel.setOnClickListener {
            handleBackPressed()
        }
    }

    private fun validationAndSubmit() {
        if (binding.etTelephoneNumber.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter telephone number", Toast.LENGTH_SHORT).show()
            return
        } else if (Utils.isValidEthiopiaLocalNumber(
                binding.etTelephoneNumber.text.toString().trim()
            )
        ) {
            Toast.makeText(this, "Please enter valid telephone number", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.etCity.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter city", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.etPostalCode.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter postal code", Toast.LENGTH_SHORT).show()
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
            customerDetail!!.telexNo = binding.etTelephoneNumber.text.toString().trim()
            customerDetail!!.city = binding.etCity.text.toString().trim()
            customerDetail!!.address = binding.etAddress.text.toString().trim()
            //customerDetail!!.postalCode = binding.etPostalCode.text.toString().trim()
            ProgressDialog.start(this@UpdateCustomerActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.updateCustomer(
                CustomerUpdate(
                    binding.etAddress.text.toString().trim(),

                    binding.etCity.text.toString().trim(),
                    AlvimaTuckApplication.latitude,
                    AlvimaTuckApplication.longitude,
                    customerDetail!!.no,
                    binding.etPostalCode.text.toString().trim(),
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