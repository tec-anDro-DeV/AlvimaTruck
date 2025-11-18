package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityViewCustomerBinding

class ViewCustomerActivity : BaseActivity<ActivityViewCustomerBinding>() {
    override fun inflateBinding(): ActivityViewCustomerBinding {
        return ActivityViewCustomerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.btnEdit.setOnClickListener {
            startActivity(Intent(this, UpdateCustomerActivity::class.java))
        }
    }
}