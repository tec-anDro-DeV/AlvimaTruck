package com.alvimatruck.activity

import android.os.Bundle
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityHomeBinding

class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    override fun inflateBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.rlBottomHome.setOnClickListener {
            binding.rlBottomHome.setBackgroundResource(R.drawable.orange_circle)
            binding.rlBottomTrip.setBackgroundResource(0)
            binding.rlBottomVanStock.setBackgroundResource(0)
            binding.rlBottomProfile.setBackgroundResource(0)
        }

        binding.rlBottomTrip.setOnClickListener {
            binding.rlBottomHome.setBackgroundResource(0)
            binding.rlBottomTrip.setBackgroundResource(R.drawable.orange_circle)
            binding.rlBottomVanStock.setBackgroundResource(0)
            binding.rlBottomProfile.setBackgroundResource(0)
        }


        binding.rlBottomVanStock.setOnClickListener {
            binding.rlBottomHome.setBackgroundResource(0)
            binding.rlBottomTrip.setBackgroundResource(0)
            binding.rlBottomVanStock.setBackgroundResource(R.drawable.orange_circle)
            binding.rlBottomProfile.setBackgroundResource(0)
        }


        binding.rlBottomProfile.setOnClickListener {
            binding.rlBottomHome.setBackgroundResource(0)
            binding.rlBottomTrip.setBackgroundResource(0)
            binding.rlBottomVanStock.setBackgroundResource(0)
            binding.rlBottomProfile.setBackgroundResource(R.drawable.orange_circle)
        }


    }
}