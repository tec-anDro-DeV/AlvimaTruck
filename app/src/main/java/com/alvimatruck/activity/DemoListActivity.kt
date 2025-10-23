package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.DemoListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityDemoListBinding
import com.alvimatruck.utils.Utils
import com.intuit.sdp.R

class DemoListActivity : BaseActivity<ActivityDemoListBinding>() {

    private var demoListAdapter: DemoListAdapter? = null

    override fun inflateBinding(): ActivityDemoListBinding {
        return ActivityDemoListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.rvList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(R.dimen._10sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
        binding.rvList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        demoListAdapter = DemoListAdapter(
            this@DemoListActivity, Utils.getDummyArrayList(15)
        )
        binding.rvList.adapter = demoListAdapter

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

    }

}