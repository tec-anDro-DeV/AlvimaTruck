package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityDemoBinding


class DemoActivity : BaseActivity<ActivityDemoBinding>() {


    override fun inflateBinding(): ActivityDemoBinding {
        return ActivityDemoBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnOpenMap.setOnClickListener {
            startActivity(Intent(this, MapPolygonActivity::class.java))
        }
        binding.btnOpenRoutePath.setOnClickListener {
            startActivity(Intent(this, MapRouteActivity::class.java))
        }

        binding.btnOpenList.setOnClickListener {
            startActivity(Intent(this, DemoListActivity::class.java))
        }
    }


}

