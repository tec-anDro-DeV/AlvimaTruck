package com.alvimatruck.activity

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.R
import com.alvimatruck.adapter.SalesReportListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivitySalesReportBinding
import com.alvimatruck.utils.Utils
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.archit.calendardaterangepicker.customviews.DateRangeCalendarView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SalesReportActivity : BaseActivity<ActivitySalesReportBinding>() {
    private var salesReportListAdapter: SalesReportListAdapter? = null


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

        binding.rvItems.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
        binding.rvItems.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        salesReportListAdapter = SalesReportListAdapter(
            this@SalesReportActivity, Utils.getDummyArrayList(5)
        )
        binding.rvItems.adapter = salesReportListAdapter

    }

    private fun openDateRangePicker() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_range_picker)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        val calendarView = dialog.findViewById<DateRangeCalendarView>(R.id.dateRangeCalendar)
        val btnCancel = dialog.findViewById<TextView>(R.id.btnCancel)
        val btnOk = dialog.findViewById<TextView>(R.id.btnOk)

        var startDate: Calendar? = null
        var endDate: Calendar? = null

        // Listener for range selection
        calendarView.setCalendarListener(object : CalendarListener {
            override fun onDateRangeSelected(start: Calendar, end: Calendar) {
                startDate = start
                endDate = end
            }

            override fun onFirstDateSelected(start: Calendar) {
                // User selected first date
            }
        })

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnOk.setOnClickListener {
            if (startDate != null && endDate != null) {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val start = sdf.format(startDate!!.time)
                val end = sdf.format(endDate!!.time)

                binding.tvDateRangePicker.text = "$start — $end"
                dialog.dismiss()
            } else {
                Toast.makeText(
                    this, getString(R.string.please_select_a_date_range), Toast.LENGTH_SHORT
                ).show()
            }
        }

        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }
}