package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.R
import com.alvimatruck.adapter.VanStockListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityVanStockBinding
import com.alvimatruck.utils.Utils

class VanStockActivity : BaseActivity<ActivityVanStockBinding>() {
    private var vanStockListAdapter: VanStockListAdapter? = null

    override fun inflateBinding(): ActivityVanStockBinding {
        return ActivityVanStockBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rlBottomHome.setOnClickListener {
            handleBackPressed()
            //            binding.rlBottomHome.setBackgroundResource(R.drawable.orange_circle)
//            binding.rlBottomTrip.setBackgroundResource(0)
//            binding.rlBottomVanStock.setBackgroundResource(0)
//            binding.rlBottomOpreation.setBackgroundResource(0
        }

        binding.rlBottomVanStock.setOnClickListener {
//            binding.rlBottomHome.setBackgroundResource(0)
//            binding.rlBottomTrip.setBackgroundResource(0)
//            binding.rlBottomVanStock.setBackgroundResource(R.drawable.orange_circle)
//            binding.rlBottomOpreation.setBackgroundResource(0)
        }

        binding.rlBottomTrip.setOnClickListener {
            binding.rlBottomHome.setBackgroundResource(0)
            binding.rlBottomTrip.setBackgroundResource(R.drawable.orange_circle)
            binding.rlBottomVanStock.setBackgroundResource(0)
            binding.rlBottomOpreation.setBackgroundResource(0)
        }

        binding.rlBottomOpreation.setOnClickListener {
            binding.rlBottomHome.setBackgroundResource(0)
            binding.rlBottomTrip.setBackgroundResource(0)
            binding.rlBottomVanStock.setBackgroundResource(0)
            binding.rlBottomOpreation.setBackgroundResource(R.drawable.orange_circle)
        }


        binding.rvStockList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
        binding.rvStockList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        vanStockListAdapter = VanStockListAdapter(
            this@VanStockActivity, Utils.getDummyArrayList(5)
        )
        binding.rvStockList.adapter = vanStockListAdapter

    }
}