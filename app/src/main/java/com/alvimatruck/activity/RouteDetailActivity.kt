package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_orange)
                binding.tvStartEndTrip.text = "End Trip"
                binding.tvPendingCustomer.text = "4"
                binding.tvTotalVisitedCustomer.text = "30"
                binding.progressBar.progress = 88
                binding.rlStartKilometer.visibility = View.VISIBLE
                binding.rlEndKilometer.visibility = View.GONE
                binding.llBottomButtons.visibility = View.VISIBLE

            } else {
                binding.tvStatus.text = "Completed"
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_green)
                binding.tvPendingCustomer.text = "4"
                binding.tvTotalVisitedCustomer.text = "30"
                binding.progressBar.progress = 88
                binding.rlStartKilometer.visibility = View.VISIBLE
                binding.rlEndKilometer.visibility = View.VISIBLE
                binding.llBottomButtons.visibility = View.GONE

            }
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

        binding.llCustomer.setOnClickListener {

            startActivity(Intent(this, CustomersActivity::class.java))

        }
    }
}