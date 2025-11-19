package com.alvimatruck.activity

import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivitySellOrderDetailBinding

class SellOrderDetailActivity : BaseActivity<ActivitySellOrderDetailBinding>() {
    override fun inflateBinding(): ActivitySellOrderDetailBinding {
        return ActivitySellOrderDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}