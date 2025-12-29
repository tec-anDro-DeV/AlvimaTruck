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
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityDeliveryRouteDetailBinding
import com.alvimatruck.model.responses.RouteDetail

class DeliveryRouteDetailActivity : BaseActivity<ActivityDeliveryRouteDetailBinding>() {
    var status: String? = ""

    var routeDetail: RouteDetail? = null
    var isChange: Boolean = false


    override fun inflateBinding(): ActivityDeliveryRouteDetailBinding {
        return ActivityDeliveryRouteDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.tvViewMap.setOnClickListener {
//            startActivity(
//                Intent(this, RouteMapActivity::class.java).putExtra(
//                    Constants.RouteDetail, Gson().toJson(routeDetail)
//                )
//            )
        }

        binding.tvStartEndTrip.setOnClickListener {
            if (status.equals("Pending")) {
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
                        // startTripAPI(etStartKm.text.toString())
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
                            .toInt() <= binding.tvVanStartKilometer.text.toString().toInt()
                    ) {
                        Toast.makeText(
                            this,
                            getString(R.string.end_km_should_be_greater_than_start_km),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        dialog.dismiss()
                        //endTripAPI(etEndKm.text.toString())
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
                            this@DeliveryRouteDetailActivity, R.color.black
                        )
                    )
                    buttonTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@DeliveryRouteDetailActivity, R.color.orange
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
                                this@DeliveryRouteDetailActivity, R.color.gray
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
                            Toast.makeText(
                                this, getString(R.string.please_write_a_reason), Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener // Stop execution, don't dismiss dialog
                        }
                        finalReason = writtenReason
                    }

                    Log.d("TAG", "Selected/Written Reason: $finalReason")
                    dialog.dismiss()
                    // cancelTripAPI(finalReason)
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

        binding.llDeliveries.setOnClickListener {
            var tripStart = false
            tripStart = status == "InProgress"

            startActivity(
                Intent(
                    this, DeliveryListActivity::class.java
                )
//                    .putExtra(Constants.RouteDetail, routeDetail!!.routeName)
//                    .putExtra(Constants.TripStart, tripStart)
            )

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