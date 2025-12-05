package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityDriverHomeBinding
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson

class DriverHomeActivity : BaseActivity<ActivityDriverHomeBinding>() {
    var userDetail: UserDetail? = null

    override fun inflateBinding(): ActivityDriverHomeBinding {
        return ActivityDriverHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvDate.text = Utils.getFullDate(System.currentTimeMillis())

        userDetail =
            Gson().fromJson(SharedHelper.getKey(this, Constants.UserDetail), UserDetail::class.java)
        binding.tvUsername.text = userDetail?.firstName + " " + userDetail?.lastName

        binding.rlDelivery.setOnClickListener {
            startActivity(Intent(this, DeliveryListActivity::class.java))
        }

        binding.rlTransfer.setOnClickListener {
            startActivity(Intent(this, TransferRequestActivity::class.java))
        }

        binding.rlLogout.setOnClickListener {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_logout, null)

            val btnNo = alertLayout.findViewById<TextView>(R.id.btnNo)
            val btnYes = alertLayout.findViewById<TextView>(R.id.btnYes)

            val dialog =
                AlertDialog.Builder(this).setView(alertLayout).setCancelable(false).create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


            btnNo.setOnClickListener {
                dialog.dismiss()
            }
            btnYes.setOnClickListener {
                dialog.dismiss()
                Utils.logout(this)

            }
            dialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        binding.rlFeetManagement.setOnClickListener {
            startActivity(Intent(this, FleetManagementActivity::class.java))
        }

        binding.tvStartEndTrip.setOnClickListener {
            if (binding.tvStartEndTrip.text.equals("Start Trip")) {
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
                    binding.tvStartEndTrip.text = "End Trip"

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
                    binding.tvStartEndTrip.text = "Start Trip"

                }
                dialog.show()
                val width =
                    (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
                dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
            }
        }


    }
}