package com.alvimatruck.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isNotEmpty
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityDeliveryOrderDetailBinding
import com.alvimatruck.service.AlvimaTuckApplication
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.Utils
import com.alvimatruck.utils.Utils.distanceInKm

class DeliveryOrderDetailActivity : BaseActivity<ActivityDeliveryOrderDetailBinding>() {
    override fun inflateBinding(): ActivityDeliveryOrderDetailBinding {
        return ActivityDeliveryOrderDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndStartLocationService()
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.tvConfirmDelivery.setOnClickListener {
            startActivity(Intent(this, ConfirmDeliveryActivity::class.java))
        }

        binding.tvViewMap.setOnClickListener {
            startActivity(
                Intent(this, MapRouteActivity::class.java).putExtra(
                    Constants.LATITUDE, 23.001438039330147
                ).putExtra(Constants.LONGITUDE, 72.5509716370138)
                    .putExtra(Constants.CustomerDetail, "")
            )
        }

        if (binding.tvStatus.text.equals("Open")) {
            binding.tvStartEndTrip.text = getString(R.string.start_trip)
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_red)
            binding.rlStartKilometer.visibility = View.GONE
            binding.rlEndKilometer.visibility = View.GONE
            binding.llBottomButtons.visibility = View.GONE
            binding.tvStartEndTrip.visibility = View.VISIBLE

        } else if (binding.tvStatus.text.equals("InProgress")) {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_orange)
            binding.rlStartKilometer.visibility = View.VISIBLE
            binding.rlEndKilometer.visibility = View.GONE
            binding.llBottomButtons.visibility = View.VISIBLE
            binding.tvStartEndTrip.visibility = View.GONE

        } else if (binding.tvStatus.text.equals("Cancelled")) {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_red)
            binding.rlStartKilometer.visibility = View.VISIBLE
            binding.rlEndKilometer.visibility = View.VISIBLE
            binding.llBottomButtons.visibility = View.GONE
            binding.tvStartEndTrip.visibility = View.GONE
        } else {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_green)
            binding.rlStartKilometer.visibility = View.VISIBLE
            binding.rlEndKilometer.visibility = View.GONE
            binding.llBottomButtons.visibility = View.GONE
            binding.tvStartEndTrip.visibility = View.VISIBLE
            binding.tvStartEndTrip.text = getString(R.string.end_trip)
        }


        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvDistanceValue.text = distanceInKm(
                AlvimaTuckApplication.latitude,
                AlvimaTuckApplication.longitude, 23.001438039330147, 72.5509716370138
            ).toString() + " Km"

        }, 3000)



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

            val reasonsList = listOf(
                "Customer not available / Store closed",
                "Wrong address / Location not found",
                "Customer refused to accept",
                "Delivery cancelled"
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
                dialog.dismiss()

                val selectedId = rgReason.checkedRadioButtonId
                val selectedRb = alertLayout.findViewById<RadioButton>(selectedId)
                val selectedReason = selectedRb.text.toString()
                Log.d("TAG", "Selected: $selectedReason")
            }
            dialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun endTripAPI(toString: String) {


    }

    private fun startTripAPI(toString: String) {


    }
}
