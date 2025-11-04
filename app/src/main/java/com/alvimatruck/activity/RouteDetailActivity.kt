package com.alvimatruck.activity

import android.os.Bundle
import android.view.View
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityRouteDetailBinding
import com.alvimatruck.utils.Constants

class RouteDetailActivity : BaseActivity<ActivityRouteDetailBinding>() {
    var status: String? = ""
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

            if (status.equals("Pending")) {
                binding.tvStartEndTrip.text = "Start Trip"
                binding.tvStatus.text = "Pending"
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_red)
                binding.tvPendingCustomer.text = "34"
                binding.tvTotalVisitedCustomer.text = "0"
                binding.progressBar.progress = 0
                binding.rlStartKilometer.visibility = View.GONE
                binding.rlEndKilometer.visibility = View.GONE
                binding.llBottomButtons.visibility = View.VISIBLE

            } else if (status.equals("In Progress")) {
                binding.tvStatus.text = "In Progress"
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_green)
                binding.tvStartEndTrip.text = "End Trip"
                binding.tvPendingCustomer.text = "4"
                binding.tvTotalVisitedCustomer.text = "30"
                binding.progressBar.progress = 88
                binding.rlStartKilometer.visibility = View.VISIBLE
                binding.rlEndKilometer.visibility = View.GONE
                binding.llBottomButtons.visibility = View.VISIBLE

            } else {

                binding.tvStatus.text = "Completed"
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_orange)
                binding.tvPendingCustomer.text = "4"
                binding.tvTotalVisitedCustomer.text = "30"
                binding.progressBar.progress = 88
                binding.rlStartKilometer.visibility = View.VISIBLE
                binding.rlEndKilometer.visibility = View.VISIBLE
                binding.llBottomButtons.visibility = View.GONE

            }
        }


    }
}