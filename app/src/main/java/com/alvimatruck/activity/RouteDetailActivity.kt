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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isNotEmpty
import com.alvimatruck.R
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityRouteDetailBinding
import com.alvimatruck.model.request.CancelTripRequest
import com.alvimatruck.model.request.EndTripRequest
import com.alvimatruck.model.request.StartTripRequest
import com.alvimatruck.model.responses.RouteDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RouteDetailActivity : BaseActivity<ActivityRouteDetailBinding>() {
    var status: String? = ""

    var routeDetail: RouteDetail? = null
    var isChange: Boolean = false

    override fun inflateBinding(): ActivityRouteDetailBinding {
        return ActivityRouteDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        if (intent != null) {
            status = intent.getStringExtra(Constants.Status).toString()
            routeDetail = Gson().fromJson(
                intent.getStringExtra(Constants.RouteDetail).toString(),
                RouteDetail::class.java
            )

            binding.tvRouteId.text = routeDetail!!.routeName
            binding.tvRegularCustomersValue.text = routeDetail!!.regularCustomerCount.toString()
            binding.tvVisitedCustomersValue.text = routeDetail!!.visited.toString()
            binding.tvSkippedCustomersValue.text = routeDetail!!.skipped.toString()
            binding.tvTotalVisitedCustomer.text = routeDetail!!.visited.toString()
            binding.tvTotalCustomer.text = "/" + routeDetail!!.regularCustomerCount.toString()
            binding.tvPendingCustomer.text =
                (routeDetail!!.regularCustomerCount - routeDetail!!.visited).toString()

            binding.progressBar.progress =
                routeDetail!!.visited * 100 / routeDetail!!.regularCustomerCount
            binding.tvStatus.text = routeDetail!!.status
            binding.tvDistanceValue.text = routeDetail!!.distance.toString() + " Km"
            binding.tvVanStartKilometer.text = routeDetail!!.startKm.toString()
            binding.tvEndKilometer.text = routeDetail!!.endKm.toString()
            binding.tvTotalSaleValue.text = "$" + routeDetail!!.totalSalesValues.toString()


            if (status.equals("Pending")) {
                binding.tvStartEndTrip.text = "Start Trip"
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_red)
                binding.rlStartKilometer.visibility = View.GONE
                binding.rlEndKilometer.visibility = View.GONE
                binding.llBottomButtons.visibility = View.VISIBLE

            } else if (status.equals("InProgress")) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_orange)
                binding.tvStartEndTrip.text = "End Trip"
                binding.rlStartKilometer.visibility = View.VISIBLE
                binding.rlEndKilometer.visibility = View.GONE
                binding.llBottomButtons.visibility = View.VISIBLE

            } else {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_green)
                binding.rlStartKilometer.visibility = View.VISIBLE
                binding.rlEndKilometer.visibility = View.VISIBLE
                binding.llBottomButtons.visibility = View.GONE

            }
        }

        binding.tvViewMap.setOnClickListener {
            startActivity(
                Intent(this, RouteMapActivity::class.java).putExtra(
                    Constants.RouteDetail, Gson().toJson(routeDetail)
                )
            )
        }

        binding.tvStartEndTrip.setOnClickListener {
            if (status.equals("Pending")) {
                val inflater = layoutInflater
                val alertLayout = inflater.inflate(R.layout.dialog_start_trip, null)

                val etStartKm = alertLayout.findViewById<EditText>(R.id.etStartKm)
                val btnCancel = alertLayout.findViewById<TextView>(R.id.btnCancel)
                val btnSubmit = alertLayout.findViewById<TextView>(R.id.btnSubmit)


                val dialog = AlertDialog.Builder(this)
                    .setView(alertLayout)
                    .setCancelable(false)
                    .create()
                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }
                btnSubmit.setOnClickListener {
                    if (etStartKm.text.toString().isEmpty()) {
                        Toast.makeText(this, "Please enter start km", Toast.LENGTH_SHORT).show()
                    } else {
                        dialog.dismiss()
                        startTripAPI(etStartKm.text.toString())
                    }

                }
                dialog.show()
                val width =
                    (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
                dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
            } else {
                val inflater = layoutInflater
                val alertLayout = inflater.inflate(R.layout.dialog_end_trip, null)

                val etEndKm = alertLayout.findViewById<EditText>(R.id.etEndKm)
                val btnCancel = alertLayout.findViewById<TextView>(R.id.btnCancel)
                val btnSubmit = alertLayout.findViewById<TextView>(R.id.btnSubmit)


                val dialog = AlertDialog.Builder(this)
                    .setView(alertLayout)
                    .setCancelable(false)
                    .create()
                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }
                btnSubmit.setOnClickListener {
                    if (etEndKm.text.toString().isEmpty()) {
                        Toast.makeText(this, "Please enter end km", Toast.LENGTH_SHORT)
                    } else if (etEndKm.text.toString()
                            .toInt() <= binding.tvVanStartKilometer.text.toString().toInt()
                    ) {
                        Toast.makeText(
                            this,
                            "End km should be greater than start km",
                            Toast.LENGTH_SHORT
                        )
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


        binding.tvCancelRoute.setOnClickListener {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_cancel_route, null)

            val btnCancel = alertLayout.findViewById<TextView>(R.id.btnCancel)
            val btnSubmit = alertLayout.findViewById<TextView>(R.id.btnSubmit)
            val tvReasonLabel = alertLayout.findViewById<TextView>(R.id.tvReasonLabel)
            val etAddReason = alertLayout.findViewById<EditText>(R.id.etAddReason)

            val rgReason = alertLayout.findViewById<RadioGroup>(R.id.rgReason)

            val reasonsList = listOf(
                "Vehicle Breakdown",
                "Emergency",
                "Adverse Weather",
                "Route Blocked",
                "Unexpected Delay",
                "Health/Medical Issue",
                "Fuel/Technical Issue",
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
                            this@RouteDetailActivity,
                            R.color.black
                        )
                    )
                    buttonTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@RouteDetailActivity,
                            R.color.orange
                        )
                    )
                    setPadding(padding, padding, padding, padding)
                    typeface = typefaceRegular
                    setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        textSize
                    ) // Uncomment if you want exact SSP sizing logic
                    layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
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
                                this@RouteDetailActivity,
                                R.color.gray
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


            val dialog = AlertDialog.Builder(this)
                .setView(alertLayout)
                .setCancelable(false)
                .create()
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
                    cancelTripAPI(finalReason)
                } else {
                    Toast.makeText(this, "Please select a reason", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.show()
            val width =
                (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        binding.llCustomer.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    CustomersActivity::class.java
                ).putExtra(Constants.RouteDetail, routeDetail!!.routeName)
            )

        }

        binding.tvViewMap.setOnClickListener {
            startActivity(
                Intent(this, RouteMapActivity::class.java).putExtra(
                    Constants.RouteDetail,
                    Gson().toJson(routeDetail)
                )
            )
        }
    }

    private fun cancelTripAPI(reason: String) {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@RouteDetailActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.cancelTrip(
                CancelTripRequest(
                    routeDetail!!.routeName, reason

                )
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@RouteDetailActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@RouteDetailActivity,
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
                            this@RouteDetailActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@RouteDetailActivity,
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

            ProgressDialog.start(this@RouteDetailActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.startTrip(
                StartTripRequest(
                    routeDetail!!.routeName, startKm.toInt()
                )
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@RouteDetailActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@RouteDetailActivity,
                                response.body()!!.get("message").toString().replace('"', ' ')
                                    .trim(),
                                Toast.LENGTH_SHORT
                            ).show()
                            isChange = true
                            binding.tvVanStartKilometer.text = startKm
                            binding.tvStatus.text = "InProgress"
                            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_orange)
                            binding.tvStartEndTrip.text = "End Trip"
                            binding.rlStartKilometer.visibility = View.VISIBLE
                            binding.rlEndKilometer.visibility = View.GONE
                            binding.llBottomButtons.visibility = View.VISIBLE


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@RouteDetailActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@RouteDetailActivity,
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

            ProgressDialog.start(this@RouteDetailActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.endTrip(
                EndTripRequest(
                    routeDetail!!.routeName, endKm.toInt()
                )
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@RouteDetailActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@RouteDetailActivity,
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
                            this@RouteDetailActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@RouteDetailActivity,
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
}