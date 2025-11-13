package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
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
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }


        binding.rlBottomTrip.setOnClickListener {

        }

        binding.rlBottomOpreation.setOnClickListener {
            startActivity(Intent(this@VanStockActivity, OperationsActivity::class.java))
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