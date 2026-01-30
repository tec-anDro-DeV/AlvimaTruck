package com.alvimatruck.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
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
import com.alvimatruck.databinding.ActivityDeliveryOrderDetailBinding
import com.alvimatruck.model.request.DeliveryCancelRequest
import com.alvimatruck.model.request.DeliveryEndRequest
import com.alvimatruck.model.request.DeliveryStartRequest
import com.alvimatruck.model.responses.DeliveryTripDetail
import com.alvimatruck.service.AlvimaTuckApplication
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.alvimatruck.utils.Utils.distanceInKm
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryOrderDetailActivity : BaseActivity<ActivityDeliveryOrderDetailBinding>() {
    var orderDetail: DeliveryTripDetail? = null
    private lateinit var distanceRunnable: Runnable
    private val distanceHandler = Handler(Looper.getMainLooper())

    var isChange: Boolean = false
    override fun inflateBinding(): ActivityDeliveryOrderDetailBinding {
        return ActivityDeliveryOrderDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndStartLocationService()
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        if (intent != null) {
            orderDetail = Gson().fromJson(
                intent.getStringExtra(Constants.DeliveryDetail).toString(),
                DeliveryTripDetail::class.java
            )
            distanceRunnable = object : Runnable {
                override fun run() {
                    val currentLat = AlvimaTuckApplication.latitude
                    val currentLng = AlvimaTuckApplication.longitude
                    val destLat = orderDetail?.latitude ?: 0.0
                    val destLng = orderDetail?.longitude ?: 0.0

                    // Check if all 4 data points are available (not zero)
                    if (currentLat != 0.0 && currentLng != 0.0 && destLat != 0.0 && destLng != 0.0) {
                        val distance = distanceInKm(currentLat, currentLng, destLat, destLng)
                        binding.tvDistanceValue.text = "$distance Km"
                    } else {
                        Log.d("DistanceUpdate", "Coordinates not yet available")
                    }

                    // Schedule the next execution in 5 seconds
                    distanceHandler.postDelayed(this, 5000)
                }
            }
            distanceHandler.postDelayed(distanceRunnable, 1000)

            binding.tvStatus.text = orderDetail!!.appStatus
            binding.tvOrderId.text = orderDetail!!.no
            binding.tvCustomerName.text = orderDetail!!.sellToCustomerName
            binding.tvAddress.text =
                orderDetail!!.shipToAddress + " " + orderDetail!!.shipToPostCode
            Utils.loadProfileWithPlaceholder(
                this, binding.ivUser, orderDetail!!.sellToCustomerName, ""
            )
            binding.tvOrderDate.text = Utils.getFormatedRequestDate(orderDetail!!.orderDate)
            binding.tvVanStartKilometer.text = orderDetail!!.startKM.toString()
            binding.tvEndKilometer.text = orderDetail!!.endKM.toString()
            binding.tvContactNumber.text = orderDetail!!.getFormattedContactNo()


            binding.llItemList.removeAllViews()

            for (i in orderDetail!!.postedSalesShipmentLines.indices) {

                val item = orderDetail!!.postedSalesShipmentLines[i]

                // ✅ Inflate item_product.xml
                val productView = LayoutInflater.from(this)
                    .inflate(R.layout.item_product, binding.llItemList, false)

                // ✅ Find Views
                val tvProductName = productView.findViewById<TextView>(R.id.tvProductName)
                val tvProductDetails = productView.findViewById<TextView>(R.id.tvProductDetails)
                val dividerLine = productView.findViewById<View>(R.id.dividerLine)

                // ✅ Set Dynamic Data
                tvProductName.text = "${item.description}"
                tvProductDetails.text =
                    "Qty: ${item.quantity} ${item.unitOfMeasure} • SKU: ${item.no}"

                // ✅ Hide Divider for Last Item
                if (i == orderDetail!!.postedSalesShipmentLines.size - 1) {
                    dividerLine.visibility = View.GONE
                }

                // ✅ Add Inflated View into LinearLayout
                binding.llItemList.addView(productView)
            }
        }

        binding.tvConfirmDelivery.setOnClickListener {
            val intent =
                Intent(this, ConfirmDeliveryActivity::class.java).putExtra(
                    Constants.OrderID,
                    orderDetail!!.orderNo
                )
            startForResult.launch(intent)
        }

        binding.tvViewMap.setOnClickListener {
            startActivity(
                Intent(this, MapRouteActivity::class.java).putExtra(
                    Constants.LATITUDE, orderDetail!!.latitude
                ).putExtra(Constants.LONGITUDE, orderDetail!!.longitude)
                    .putExtra(Constants.CustomerDetail, orderDetail!!.sellToCustomerName)
            )
        }

        when (orderDetail!!.appStatus) {
            "Open" -> {
                binding.tvStartEndTrip.text = getString(R.string.start_trip)
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_red)
                binding.rlStartKilometer.visibility = View.GONE
                binding.rlEndKilometer.visibility = View.GONE
                binding.llBottomButtons.visibility = View.GONE
                binding.tvStartEndTrip.visibility = View.VISIBLE
                binding.rlDistance.visibility = View.VISIBLE
            }

            "InProgress" -> {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_orange)
                binding.rlStartKilometer.visibility = View.VISIBLE
                binding.rlEndKilometer.visibility = View.GONE
                binding.llBottomButtons.visibility = View.VISIBLE
                binding.tvStartEndTrip.visibility = View.GONE
                binding.rlDistance.visibility = View.VISIBLE
            }

            "Cancelled" -> {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_red)
                binding.rlStartKilometer.visibility = View.VISIBLE
                binding.rlEndKilometer.visibility = View.VISIBLE
                binding.llBottomButtons.visibility = View.GONE
                binding.tvStartEndTrip.visibility = View.GONE
                binding.rlDistance.visibility = View.GONE
            }

            "Delivered" -> {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_green)
                binding.rlStartKilometer.visibility = View.VISIBLE
                binding.rlEndKilometer.visibility = View.GONE
                binding.llBottomButtons.visibility = View.GONE
                binding.tvStartEndTrip.visibility = View.VISIBLE
                binding.tvStartEndTrip.text = getString(R.string.end_trip)
                binding.rlDistance.visibility = View.GONE
            }

            else -> {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_green)
                binding.rlStartKilometer.visibility = View.VISIBLE
                binding.rlEndKilometer.visibility = View.VISIBLE
                binding.llBottomButtons.visibility = View.GONE
                binding.tvStartEndTrip.visibility = View.GONE
                binding.rlDistance.visibility = View.GONE
            }
        }



        binding.tvStartEndTrip.setOnClickListener {
            if (binding.tvStatus.text.equals("Open")) {
                if (Utils.isTripInProgress) {
                    Toast.makeText(
                        this,
                        getString(R.string.you_can_t_start_a_new_trip_while_another_trip_is_in_progress),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val inflater = layoutInflater
                    val alertLayout = inflater.inflate(R.layout.dialog_start_trip, null)

                    val etStartKm = alertLayout.findViewById<EditText>(R.id.etStartKm)
                    val btnCancel = alertLayout.findViewById<TextView>(R.id.btnCancel)
                    val btnSubmit = alertLayout.findViewById<TextView>(R.id.btnSubmit)


                    val dialog =
                        AlertDialog.Builder(this).setView(alertLayout).setCancelable(false).create()
                    dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


                    btnCancel.setOnClickListener {
                        dialog.dismiss()
                    }
                    btnSubmit.setOnClickListener {
                        if (etStartKm.text.toString().isEmpty()) {
                            Toast.makeText(
                                this, getString(R.string.please_enter_start_km), Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            dialog.dismiss()
                            startTripAPI(etStartKm.text.toString())
                        }

                    }
                    dialog.show()
                    val width =
                        (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
                    dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
                }
            } else {
                val inflater = layoutInflater
                val alertLayout = inflater.inflate(R.layout.dialog_end_trip, null)

                val etEndKm = alertLayout.findViewById<EditText>(R.id.etEndKm)
                val btnCancel = alertLayout.findViewById<TextView>(R.id.btnCancel)
                val btnSubmit = alertLayout.findViewById<TextView>(R.id.btnSubmit)


                val dialog =
                    AlertDialog.Builder(this).setView(alertLayout).setCancelable(false).create()
                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }
                btnSubmit.setOnClickListener {
                    if (etEndKm.text.toString().isEmpty()) {
                        Toast.makeText(
                            this, getString(R.string.please_enter_end_km), Toast.LENGTH_SHORT
                        ).show()
                    } else if (etEndKm.text.toString()
                            .toInt() < binding.tvVanStartKilometer.text.toString().toInt()
                    ) {
                        Toast.makeText(
                            this,
                            getString(R.string.end_km_should_be_greater_than_start_km),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        dialog.dismiss()
                        endTripAPI(etEndKm.text.toString())
                    }

                }
                dialog.show()
                val width =
                    (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
                dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
            }
        }

        binding.tvVisited.setOnClickListener {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_no_handover, null)

            val btnCancel = alertLayout.findViewById<TextView>(R.id.btnCancel)
            val btnSubmit = alertLayout.findViewById<TextView>(R.id.btnSubmit)
            val rgReason = alertLayout.findViewById<RadioGroup>(R.id.rgReason)
            val tvReasonLabel = alertLayout.findViewById<TextView>(R.id.tvReasonLabel)
            val etAddReason = alertLayout.findViewById<EditText>(R.id.etAddReason)
            val etEndKm = alertLayout.findViewById<EditText>(R.id.etEndKm)


            val reasonsList = listOf(
                "Customer not available / Store closed",
                "Wrong address / Location not found",
                "Customer refused to accept",
                "Delivery cancelled",
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
                            this@DeliveryOrderDetailActivity, R.color.black
                        )
                    )
                    buttonTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@DeliveryOrderDetailActivity, R.color.orange
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
                                this@DeliveryOrderDetailActivity, R.color.gray
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

                    if (selectedOption == "Other") {
                        val writtenReason = etAddReason.text.toString().trim()
                        if (writtenReason.isEmpty()) {
                            Toast.makeText(
                                this, getString(R.string.please_write_a_reason), Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener // Stop execution, don't dismiss dialog
                        }
                        finalReason = writtenReason
                    } else if (etEndKm.text.toString().isEmpty()) {
                        Toast.makeText(
                            this, getString(R.string.please_enter_end_km), Toast.LENGTH_SHORT
                        ).show()
                    } else if (etEndKm.text.toString()
                            .toInt() < binding.tvVanStartKilometer.text.toString().toInt()
                    ) {
                        Toast.makeText(
                            this, "End km should be greater than start km", Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        cancelTripAPI(finalReason, etEndKm.text.toString().toInt())
                    }
                    Log.d("TAG", "Selected/Written Reason: $finalReason")
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        this, getString(R.string.please_select_a_reason), Toast.LENGTH_SHORT
                    ).show()
                }
            }
            dialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun cancelTripAPI(reason: String, endKm: Int) {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@DeliveryOrderDetailActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.cancelDriverTrip(
                DeliveryCancelRequest(
                    orderDetail!!.orderNo,
                    reason,
                    endKm,
                    AlvimaTuckApplication.latitude.toString() + "," + AlvimaTuckApplication.longitude.toString()
                )
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@DeliveryOrderDetailActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@DeliveryOrderDetailActivity,
                                response.body()!!.get("message").toString().replace('"', ' ')
                                    .trim(),
                                Toast.LENGTH_SHORT
                            ).show()
                            isChange = true
                            handleBackPressed()

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@DeliveryOrderDetailActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@DeliveryOrderDetailActivity,
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

    private fun endTripAPI(endKm: String) {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@DeliveryOrderDetailActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.endDriverTrip(
                DeliveryEndRequest(
                    orderDetail!!.orderNo, endKm.toInt()
                )
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@DeliveryOrderDetailActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@DeliveryOrderDetailActivity,
                                response.body()!!.get("message").toString().replace('"', ' ')
                                    .trim(),
                                Toast.LENGTH_SHORT
                            ).show()
                            isChange = true
                            Utils.isTripInProgress = false
                            handleBackPressed()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@DeliveryOrderDetailActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@DeliveryOrderDetailActivity,
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

    private fun startTripAPI(startKm: String) {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@DeliveryOrderDetailActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.startDriverTrip(
                DeliveryStartRequest(
                    orderDetail!!.orderNo,
                    orderDetail!!.driverID,
                    startKm.toInt()
                )
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@DeliveryOrderDetailActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@DeliveryOrderDetailActivity,
                                response.body()!!.get("message").toString().replace('"', ' ')
                                    .trim(),
                                Toast.LENGTH_SHORT
                            ).show()
                            isChange = true
                            binding.tvVanStartKilometer.text = startKm
                            binding.tvStatus.text = "InProgress"
                            Utils.isTripInProgress = true
                            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_orange)
                            binding.rlStartKilometer.visibility = View.VISIBLE
                            binding.rlEndKilometer.visibility = View.GONE
                            binding.llBottomButtons.visibility = View.VISIBLE
                            binding.tvStartEndTrip.visibility = View.GONE
                            binding.rlDistance.visibility = View.VISIBLE

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@DeliveryOrderDetailActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@DeliveryOrderDetailActivity,
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
        if (isChange) {
            setResult(RESULT_OK)
        }
        finish()
        super.handleBackPressed(callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the loop when activity is destroyed
        if (::distanceRunnable.isInitialized) {
            distanceHandler.removeCallbacks(distanceRunnable)
        }
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            binding.tvStatus.text = "Delivered"
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_green)
            binding.rlStartKilometer.visibility = View.VISIBLE
            binding.rlEndKilometer.visibility = View.GONE
            binding.llBottomButtons.visibility = View.GONE
            binding.tvStartEndTrip.visibility = View.VISIBLE
            binding.tvStartEndTrip.text = getString(R.string.end_trip)
            binding.rlDistance.visibility = View.GONE
        }
    }
}
