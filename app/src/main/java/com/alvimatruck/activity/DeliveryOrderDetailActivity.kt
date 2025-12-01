package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityDeliveryOrderDetailBinding

class DeliveryOrderDetailActivity : BaseActivity<ActivityDeliveryOrderDetailBinding>() {
    override fun inflateBinding(): ActivityDeliveryOrderDetailBinding {
        return ActivityDeliveryOrderDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.tvConfirmDelivery.setOnClickListener {
            startActivity(Intent(this, ConfirmDeliveryActivity::class.java))
        }

        binding.tvVisited.setOnClickListener {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_no_handover, null)

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
}
