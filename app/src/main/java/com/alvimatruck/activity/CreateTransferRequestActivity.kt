package com.alvimatruck.activity

import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityCreateTransferRequestBinding
import com.alvimatruck.utils.Utils

class CreateTransferRequestActivity : BaseActivity<ActivityCreateTransferRequestBinding>() {
    override fun inflateBinding(): ActivityCreateTransferRequestBinding {
        return ActivityCreateTransferRequestBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.tvDateTime.text = Utils.getFullDateWithTime(System.currentTimeMillis())
        binding.tvTransferNumber.text = System.currentTimeMillis().toString()
    }
}