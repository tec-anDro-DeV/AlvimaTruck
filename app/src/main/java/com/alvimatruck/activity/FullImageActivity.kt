package com.alvimatruck.activity

import android.os.Bundle
import androidx.core.net.toUri
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityFullImageBinding
import com.alvimatruck.utils.Constants
import com.bumptech.glide.Glide

class FullImageActivity : BaseActivity<ActivityFullImageBinding>() {
    var uriString: String? = null
    override fun inflateBinding(): ActivityFullImageBinding {
        return ActivityFullImageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        if (intent != null) {
            uriString = intent.getStringExtra(Constants.ImageUri).toString()
            val imageUri = uriString!!.toUri()

            Glide.with(this)
                .load(imageUri)
                .into(binding.photoView)
        }
    }
}