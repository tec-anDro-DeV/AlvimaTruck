package com.alvimatruck.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isNotEmpty
import com.alvimatruck.R
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityViewCustomerBinding
import com.alvimatruck.model.request.VisitedTripRequest
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

class ViewCustomerActivity : BaseActivity<ActivityViewCustomerBinding>() {
    var customerDetail: CustomerDetail? = null
    var tripStart: Boolean = false
    override fun inflateBinding(): ActivityViewCustomerBinding {
        return ActivityViewCustomerBinding.inflate(layoutInflater)
    }


    private val openUpdateCustomer =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedCustomer =
                    Gson().fromJson(
                        result.data?.getStringExtra(Constants.CustomerDetail).toString(),
                        CustomerDetail::class.java
                    )

                if (updatedCustomer != null) {
                    customerDetail = updatedCustomer
                    showUpdatedData() // refresh UI
                }
            }
        }

    private fun showUpdatedData() {
        binding.tvCustomerName.text = customerDetail!!.searchName
        binding.tvContactNumber.text = customerDetail!!.getFormattedContactNo()
        binding.tvContactName.text = customerDetail!!.contact
        binding.tvTelephoneNumber.text = customerDetail!!.getFormattedTelephoneNo()
        binding.tvAddress.text = customerDetail!!.address
        binding.tvCity.text = customerDetail!!.city
        binding.tvPostalCode.text = customerDetail!!.postCode
        binding.tvRouteName.text = customerDetail!!.routeName
        binding.tvTINNumber.text = customerDetail!!.tinNo
        binding.tvCustomerPostingGroup.text = customerDetail!!.customerPostingGroup
        binding.tvCustomerPricingGroup.text = customerDetail!!.customerPriceGroup
        binding.tvUsages.text = customerDetail!!.balanceLcy.toString()
        binding.tvTotalLimit.text = "Total: " + customerDetail!!.creditLimitLcy.toString()
        val progress =
            if (customerDetail!!.creditLimitLcy == 0.0 || customerDetail!!.balanceLcy <= 0) {
                0
            } else {
                ((customerDetail!!.creditLimitLcy - customerDetail!!.balanceLcy / customerDetail!!.creditLimitLcy) * 100).toInt()
                    .coerceIn(0, 100)
            }

        binding.progressBar.progress = progress



        Utils.loadProfileWithPlaceholder(
            this,
            binding.ivCustomer,
            customerDetail!!.searchName,
            customerDetail!!.customerImage
        )

        if (customerDetail!!.status == "Pending" || !tripStart || customerDetail!!.visitedToday) {
            binding.llCreditLimit.visibility = View.GONE
            binding.llBottomButtons.visibility = View.GONE
        } else {
            binding.llCreditLimit.visibility = View.VISIBLE
            binding.llBottomButtons.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) {
            customerDetail = Gson().fromJson(
                intent.getStringExtra(Constants.CustomerDetail).toString(),
                CustomerDetail::class.java
            )
            tripStart = intent.getBooleanExtra(Constants.TripStart, false)
            showUpdatedData()

//            if (customerDetail!!.visitedToday) {
//                binding.tvVisited.visibility = View.GONE
//            } else {
//                binding.tvVisited.visibility = View.VISIBLE
//            }


        }

        binding.tvViewMap.setOnClickListener {
            startActivity(
                Intent(this, MapRouteActivity::class.java).putExtra(
                    Constants.LATITUDE, customerDetail!!.latitude
                ).putExtra(Constants.LONGITUDE, customerDetail!!.longitude)
                    .putExtra(Constants.CustomerDetail, customerDetail!!.searchName)
            )
        }

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.btnEdit.setOnClickListener {
            val intent = Intent(
                this,
                UpdateCustomerActivity::class.java
            ).putExtra(Constants.CustomerDetail, Gson().toJson(customerDetail))
            openUpdateCustomer.launch(intent)
        }

        binding.tvNewOrder.setOnClickListener {
            val intent =
                Intent(
                    this,
                    NewSalesActivity::class.java
                ).putExtra(Constants.CustomerDetail, Gson().toJson(customerDetail))

            openUpdateCustomer.launch(intent)
        }

        binding.tvVisited.setOnClickListener {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_visited, null)

            val btnCancel = alertLayout.findViewById<TextView>(R.id.btnCancel)
            val btnSubmit = alertLayout.findViewById<TextView>(R.id.btnSubmit)
            val tvReasonLabel = alertLayout.findViewById<TextView>(R.id.tvReasonLabel)
            val etAddReason = alertLayout.findViewById<EditText>(R.id.etAddReason)
            val rgReason = alertLayout.findViewById<RadioGroup>(R.id.rgReason)

            val reasonsList = listOf(
                "No requirement today",
                "Stock available / Low demand",
                "Price/Rate issue",
                "Owner/Decision maker not available",
                "Store closed",
                "Payment/Credit issue",
                "Next order after few days",
                "Other"

            )
            rgReason.removeAllViews()

            val typefaceRegular = ResourcesCompat.getFont(this, R.font.sansregular)
            val padding = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._11sdp)
            val textSize = resources.getDimension(com.intuit.ssp.R.dimen._11ssp)

            reasonsList.forEachIndexed { index, reason ->
                val radioButton = RadioButton(this).apply {
                    text = reason
                    id = View.generateViewId() // Generate unique ID
                    setTextColor(
                        ContextCompat.getColor(
                            this@ViewCustomerActivity, R.color.black
                        )
                    )
                    buttonTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@ViewCustomerActivity, R.color.orange
                        )
                    )
                    setPadding(padding, padding, padding, padding)
                    typeface = typefaceRegular
                    setTextSize(
                        TypedValue.COMPLEX_UNIT_PX, textSize
                    ) // Uncomment if you want exact SSP sizing logic
                    layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginStart = padding
                    }
                }

                rgReason.addView(radioButton)

                // Add Divider Line (except for the last item)
                if (index < reasonsList.size - 1) {
                    val divider = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp)
                        )
                        setBackgroundColor(
                            ContextCompat.getColor(
                                this@ViewCustomerActivity, R.color.gray
                            )
                        )
                    }
                    rgReason.addView(divider)
                }
            }
            rgReason.setOnCheckedChangeListener { group, checkedId ->
                val selectedRb = group.findViewById<RadioButton>(checkedId)
                if (selectedRb != null && selectedRb.text == "Other") {
                    tvReasonLabel.visibility = View.VISIBLE
                    etAddReason.visibility = View.VISIBLE
                    etAddReason.requestFocus()
                } else {
                    tvReasonLabel.visibility = View.GONE
                    etAddReason.visibility = View.GONE
                    // Hide keyboard if needed, or just clear focus
                    etAddReason.clearFocus()
                }
            }

            // Select the first item by default
            if (rgReason.isNotEmpty()) {
                (rgReason.getChildAt(0) as? RadioButton)?.isChecked = true
            }


            val dialog =
                AlertDialog.Builder(this).setView(alertLayout).setCancelable(false).create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            btnSubmit.setOnClickListener {
                val selectedId = rgReason.checkedRadioButtonId
                if (selectedId != -1) {
                    val selectedRb = alertLayout.findViewById<RadioButton>(selectedId)
                    val selectedOption = selectedRb.text.toString()
                    var finalReason = selectedOption

                    // If "Other" is selected, validate and use the EditText value
                    if (selectedOption == "Other") {
                        val writtenReason = etAddReason.text.toString().trim()
                        if (writtenReason.isEmpty()) {
                            Toast.makeText(this, "Please write a reason", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener // Stop execution, don't dismiss dialog
                        }
                        finalReason = writtenReason
                    }

                    Log.d("TAG", "Selected/Written Reason: $finalReason")
                    dialog.dismiss()
                    visitTripAPI(finalReason)
                } else {
                    Toast.makeText(this, "Please select a reason", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun visitTripAPI(reason: String) {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@ViewCustomerActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.visitTrip(
                VisitedTripRequest(
                    customerDetail!!.no, reason

                )
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@ViewCustomerActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@ViewCustomerActivity,
                                response.body()!!.get("message").toString().replace('"', ' ')
                                    .trim(),
                                Toast.LENGTH_SHORT
                            ).show()
                            customerDetail!!.visitedToday = true
                            handleBackPressed()


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@ViewCustomerActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@ViewCustomerActivity,
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

    override fun handleBackPressed(callback: OnBackPressedCallback?) {
        val intent = Intent()
        intent.putExtra(Constants.CustomerDetail, Gson().toJson(customerDetail))
        setResult(RESULT_OK, intent)
        finish()
        super.handleBackPressed(callback) // This will call finish()
    }

}