package com.alvimatruck.activity

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.alvimatruck.R
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityTripReportBinding
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.archit.calendardaterangepicker.customviews.DateRangeCalendarView
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TripReportActivity : BaseActivity<ActivityTripReportBinding>() {
    private var selectedStartDate: Calendar? = null
    private var selectedEndDate: Calendar? = null
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dateFormatterAPI = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun inflateBinding(): ActivityTripReportBinding {
        return ActivityTripReportBinding.inflate(layoutInflater)
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
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@TripReportActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.tripReport(startDate, endDate)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        ProgressDialog.dismiss()
                        if (response.code() == 401) {
                            Utils.forceLogout(this@TripReportActivity)  // show dialog before logout
                            return
                        }
                        if (response.isSuccessful) {
                            try {
                                Log.d("TAG", "onResponse: " + response.body().toString())
                                binding.tvTotalSaleCash.text =
                                    "ETB " + response.body()!!.getAsJsonObject("data")
                                        .get("totalCashSales")
                                        .toString()
                                binding.tvTotalCollectionCash.text =
                                    "ETB " + response.body()!!.getAsJsonObject("data")
                                        .get("totalCashCollections")
                                        .toString()
                                binding.tvTotalVisit.text =
                                    response.body()!!.getAsJsonObject("data").get("totalVisits")
                                        .toString()
                                binding.tvDistance.text =
                                    response.body()!!.getAsJsonObject("data").get("totalDistanceKm")
                                        .toString() + " Km"
                                binding.tvRouteCompleted.text =
                                    response.body()!!.getAsJsonObject("data").get("routesCompleted")
                                        .toString()

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                this@TripReportActivity,
                                Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@TripReportActivity,
                            getString(com.alvimatruck.R.string.api_fail_message),
                            Toast.LENGTH_SHORT
                        ).show()
                        ProgressDialog.dismiss()
                    }
                })
        } else {
            Toast.makeText(
                this,
                getString(com.alvimatruck.R.string.please_check_your_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
        }
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