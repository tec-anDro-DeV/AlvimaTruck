package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.RecentLogsListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityRecentLogsBinding
import com.alvimatruck.utils.Utils

class RecentLogsActivity : BaseActivity<ActivityRecentLogsBinding>() {
    private var recentLogsListAdapter: RecentLogsListAdapter? = null

    override fun inflateBinding(): ActivityRecentLogsBinding {
        return ActivityRecentLogsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rvLogs.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
        binding.rvLogs.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        recentLogsListAdapter = RecentLogsListAdapter(
            this@RecentLogsActivity, Utils.getDummyArrayList(5)
        )
        binding.rvLogs.adapter = recentLogsListAdapter
    }
}