package com.alvimatruck.activity

import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityNewSalesBinding
import com.alvimatruck.utils.Utils

class NewSalesActivity : BaseActivity<ActivityNewSalesBinding>() {
    override fun inflateBinding(): ActivityNewSalesBinding {
        return ActivityNewSalesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.tvPostingDate.text = Utils.getFullDateWithTime(System.currentTimeMillis())
        binding.tvToken.text = System.currentTimeMillis().toString()


    }
}