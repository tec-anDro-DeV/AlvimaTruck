package com.alvimatruck.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityViewCustomerBinding
import com.alvimatruck.model.responses.CustomerDetail
import com.alvimatruck.utils.Constants
import com.google.gson.Gson

class ViewCustomerActivity : BaseActivity<ActivityViewCustomerBinding>() {
    var customerDetail: CustomerDetail? = null
    override fun inflateBinding(): ActivityViewCustomerBinding {
        return ActivityViewCustomerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) {
            customerDetail = Gson().fromJson(
                intent.getStringExtra(Constants.CustomerDetail).toString(),
                CustomerDetail::class.java
            )
            binding.tvCustomerName.text = customerDetail!!.searchName
            binding.tvContactNumber.text = customerDetail!!.getFormattedContactNo()
            binding.tvContactName.text = customerDetail!!.contact
            binding.tvTelephoneNumber.text = customerDetail!!.getFormattedTelephoneNo()
            binding.tvAddress.text = customerDetail!!.address + " " + customerDetail!!.address2
            binding.tvCity.text = customerDetail!!.city
            binding.tvPostalCode.text = ""
            binding.tvTINNumber.text = customerDetail!!.tinNo
            binding.tvCustomerPostingGroup.text = customerDetail!!.customerPostingGroup
            binding.tvCustomerPricingGroup.text = customerDetail!!.customerPriceGroup

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
            startActivity(Intent(this, UpdateCustomerActivity::class.java))
        }

        binding.tvNewOrder.setOnClickListener {
            startActivity(Intent(this, NewSalesActivity::class.java))
        }

        binding.tvVisited.setOnClickListener {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_visited, null)

            val btnCancel = alertLayout.findViewById<TextView>(R.id.btnCancel)
            val btnSubmit = alertLayout.findViewById<TextView>(R.id.btnSubmit)

            val rgReason = alertLayout.findViewById<RadioGroup>(R.id.rgReason)

            val reasonsList = listOf(
                "No requirement today",
                "Stock available / Low demand",
                "Price/Rate issue",
                "Owner/Decision maker not available",
                "Store closed",
                "Payment/Credit issue",
                "Next order after few days",

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
                            this@ViewCustomerActivity,
                            R.color.black
                        )
                    )
                    buttonTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@ViewCustomerActivity,
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
                                this@ViewCustomerActivity,
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
    }
}