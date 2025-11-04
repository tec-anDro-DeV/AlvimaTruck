package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityHomeBinding
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils

class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    override fun inflateBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvDate.text = Utils.getFullDate(System.currentTimeMillis())


        binding.rlBottomHome.setOnClickListener {
            binding.rlBottomHome.setBackgroundResource(R.drawable.orange_circle)
            binding.rlBottomTrip.setBackgroundResource(0)
            binding.rlBottomVanStock.setBackgroundResource(0)
            binding.rlBottomOpreation.setBackgroundResource(0)
        }

        binding.rlBottomTrip.setOnClickListener {
            binding.rlBottomHome.setBackgroundResource(0)
            binding.rlBottomTrip.setBackgroundResource(R.drawable.orange_circle)
            binding.rlBottomVanStock.setBackgroundResource(0)
            binding.rlBottomOpreation.setBackgroundResource(0)
        }


        binding.rlBottomVanStock.setOnClickListener {
            binding.rlBottomHome.setBackgroundResource(0)
            binding.rlBottomTrip.setBackgroundResource(0)
            binding.rlBottomVanStock.setBackgroundResource(R.drawable.orange_circle)
            binding.rlBottomOpreation.setBackgroundResource(0)
        }


        binding.rlBottomOpreation.setOnClickListener {
            binding.rlBottomHome.setBackgroundResource(0)
            binding.rlBottomTrip.setBackgroundResource(0)
            binding.rlBottomVanStock.setBackgroundResource(0)
            binding.rlBottomOpreation.setBackgroundResource(R.drawable.orange_circle)
        }

        binding.rlSalesRoute.setOnClickListener {
            startActivity(Intent(this, TripRouteListActivity::class.java))
        }

        binding.rlLogout.setOnClickListener {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.alert_two_button_dialog, null)

            val tvTitle = alertLayout.findViewById<TextView>(R.id.tvTitle)
            val tvMessage = alertLayout.findViewById<TextView>(R.id.tvMessage)
            val btnNo = alertLayout.findViewById<TextView>(R.id.btnNo)
            val btnYes = alertLayout.findViewById<TextView>(R.id.btnYes)

            // Set content
            tvTitle.text = "Log out?"
            tvMessage.text = "Are you sure you want to log out of your account?"
            btnNo.text = "Cancel"
            btnYes.text = "Logout"


            val dialog = AlertDialog.Builder(this)
                .setView(alertLayout)
                .setCancelable(false)
                .create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


            btnNo.setOnClickListener {
                dialog.dismiss()
            }
            btnYes.setOnClickListener {
                dialog.dismiss()
                SharedHelper.putKey(
                    this,
                    Constants.IS_LOGIN,
                    false
                )
                SharedHelper.clearSharedPreferences(this)
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finishAffinity()

            }
            dialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }

    }
}