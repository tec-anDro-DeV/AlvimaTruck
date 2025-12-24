package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
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
        binding.tvUsername.text = userDetail?.driverFullName

        binding.rlDelivery.setOnClickListener {
            startActivity(Intent(this, DeliveryListActivity::class.java))
        }

        binding.rlRoute.setOnClickListener {
            startActivity(Intent(this, DeliveryTripRouteActivity::class.java))
        }

        binding.rlReport.setOnClickListener {
            startActivity(Intent(this, DeliveryTripReportActivity::class.java))
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
    }
}