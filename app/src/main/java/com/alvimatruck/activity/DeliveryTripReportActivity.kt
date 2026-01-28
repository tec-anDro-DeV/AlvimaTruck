package com.alvimatruck.activity

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityDeliveryTripReportBinding
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.archit.calendardaterangepicker.customviews.DateRangeCalendarView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DeliveryTripReportActivity : BaseActivity<ActivityDeliveryTripReportBinding>() {
    private var selectedStartDate: Calendar? = null
    private var selectedEndDate: Calendar? = null
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dateFormatterAPI = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


    override fun inflateBinding(): ActivityDeliveryTripReportBinding {
        return ActivityDeliveryTripReportBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupInitialDates()
        binding.tvDateRangePicker.setOnClickListener {
            openDateRangePicker()
        }



        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

    }

    private fun setupInitialDates() {
        val today = Calendar.getInstance()
        // Clone to ensure start and end are distinct objects
        selectedStartDate = today.clone() as Calendar
        selectedEndDate = today.clone() as Calendar
        updateDateText()
    }

    private fun updateDateText() {
        val start = dateFormatter.format(selectedStartDate!!.time)
        val end = dateFormatter.format(selectedEndDate!!.time)
        binding.tvDateRangePicker.text = "$start — $end"
        tripReportAPI(
            dateFormatterAPI.format(selectedStartDate!!.time),
            dateFormatterAPI.format(selectedEndDate!!.time)
        )
    }

    private fun tripReportAPI(startDate: String, endDate: String) {

    }

    private fun openDateRangePicker() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_range_picker)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        val calendarView = dialog.findViewById<DateRangeCalendarView>(R.id.dateRangeCalendar)
        val btnCancel = dialog.findViewById<TextView>(R.id.btnCancel)
        val btnOk = dialog.findViewById<TextView>(R.id.btnOk)

        val minDate = Calendar.getInstance().apply {
            set(2020, 0, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val maxDate = Calendar.getInstance()

        calendarView.setVisibleMonthRange(minDate, maxDate)
        calendarView.setSelectableDateRange(minDate, maxDate)

        calendarView.setCurrentMonth(maxDate)

        if (selectedStartDate != null && selectedEndDate != null) {
            calendarView.setSelectedDateRange(selectedStartDate!!, selectedEndDate!!)
            calendarView.setCurrentMonth(selectedEndDate!!)
        }

        var tempStart: Calendar? = selectedStartDate
        var tempEnd: Calendar? = selectedEndDate

        // Listener for range selection
        calendarView.setCalendarListener(object : CalendarListener {
            override fun onDateRangeSelected(start: Calendar, end: Calendar) {
                tempStart = start
                tempEnd = end
            }

            override fun onFirstDateSelected(start: Calendar) {
                tempStart = start
                tempEnd = null
            }
        })

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnOk.setOnClickListener {
            if (tempStart != null && tempEnd != null) {
                // Update class-level variables
                selectedStartDate = tempStart
                selectedEndDate = tempEnd

                updateDateText()
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