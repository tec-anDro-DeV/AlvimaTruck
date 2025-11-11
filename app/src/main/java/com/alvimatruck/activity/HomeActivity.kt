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
            val alertLayout = inflater.inflate(R.layout.dialog_logout, null)

            val btnNo = alertLayout.findViewById<TextView>(R.id.btnNo)
            val btnYes = alertLayout.findViewById<TextView>(R.id.btnYes)

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
                Utils.logout(this)

            }
            dialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        binding.llProgressRoute.setOnClickListener {
            startActivity(
                Intent(this, RouteDetailActivity::class.java).putExtra(
                    Constants.Status,
                    binding.tvStatus.text.toString()
                )
            )

        }
    }
}