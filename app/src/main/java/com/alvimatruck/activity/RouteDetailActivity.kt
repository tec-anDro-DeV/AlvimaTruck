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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityRouteDetailBinding
import com.alvimatruck.model.responses.RouteDetail
import com.alvimatruck.utils.Constants
import com.google.gson.Gson

class RouteDetailActivity : BaseActivity<ActivityRouteDetailBinding>() {
    var status: String? = ""

    var routeDetail: RouteDetail? = null

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
            binding.tvTotalVisitedCustomer.text =
                (routeDetail!!.visited + routeDetail!!.skipped).toString()
            binding.tvTotalCustomer.text = "/" + routeDetail!!.regularCustomerCount.toString()
            binding.tvPendingCustomer.text =
                (routeDetail!!.regularCustomerCount - routeDetail!!.visited + routeDetail!!.skipped).toString()

            binding.progressBar.progress =
                (routeDetail!!.visited + routeDetail!!.skipped) * 100 / routeDetail!!.regularCustomerCount

            if (status.equals("Pending")) {
                binding.tvStartEndTrip.text = "Start Trip"
                binding.tvStatus.text = "Pending"
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_red)
                binding.rlStartKilometer.visibility = View.GONE
                binding.rlEndKilometer.visibility = View.GONE
                binding.llBottomButtons.visibility = View.VISIBLE

            } else if (status.equals("In Progress")) {
                binding.tvStatus.text = "In Progress"
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_orange)
                binding.tvStartEndTrip.text = "End Trip"
                binding.rlStartKilometer.visibility = View.VISIBLE
                binding.rlEndKilometer.visibility = View.GONE
                binding.llBottomButtons.visibility = View.VISIBLE

            } else {
                binding.tvStatus.text = "Completed"
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
                    dialog.dismiss()

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
                    dialog.dismiss()

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

            val rgReason = alertLayout.findViewById<RadioGroup>(R.id.rgReason)

            val reasonsList = listOf(
                "Vehicle Breakdown",
                "Emergency",
                "Adverse Weather",
                "Route Blocked",
                "Unexpected Delay",
                "Health/Medical Issue",
                "Fuel/Technical Issue"
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

            // Select the first item by default
            if (rgReason.childCount > 0) {
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
                dialog.dismiss()
                val selectedId = rgReason.checkedRadioButtonId
                val selectedRb = alertLayout.findViewById<RadioButton>(selectedId)
                val selectedReason = selectedRb.text.toString()
                Log.d("TAG", "Selected: $selectedReason")
            }
            dialog.show()
            val width =
                (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        binding.llCustomer.setOnClickListener {

            startActivity(Intent(this, CustomersActivity::class.java))

        }

        binding.tvViewMap.setOnClickListener {
            startActivity(Intent(this, RouteMapActivity::class.java))
        }
    }
}