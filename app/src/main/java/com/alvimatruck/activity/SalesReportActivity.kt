package com.alvimatruck.activity

import android.os.Bundle
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivitySalesReportBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalesReportActivity : BaseActivity<ActivitySalesReportBinding>() {
    override fun inflateBinding(): ActivitySalesReportBinding {
        return ActivitySalesReportBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvDateRangePicker.setOnClickListener {
            openDateRangePicker()
        }

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

    }

    private fun openDateRangePicker() {
        // Create a Material date range picker
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(R.style.Theme_AlvimaTruck_DatePicker)
            .build()

        // show (fragment manager)
        picker.show(supportFragmentManager, "MATERIAL_DATE_RANGE_PICKER")

        // listen for positive click -> returns Pair<Long, Long> (start, end) in UTC ms
        picker.addOnPositiveButtonClickListener { selection ->
            // selection is a Pair<Long, Long> (nullable values possible)
            val startMillis = selection.first
            val endMillis = selection.second

            // Format to readable dates (local timezone)
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val start = sdf.format(Date(startMillis))
            val end = sdf.format(Date(endMillis))

            binding.tvDateRangePicker.text = "$start — $end"
        }
    }
}