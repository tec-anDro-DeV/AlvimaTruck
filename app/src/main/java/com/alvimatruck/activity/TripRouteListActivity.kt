package com.alvimatruck.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.RouteListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityTripRouteListBinding
import com.alvimatruck.utils.Utils
import com.intuit.sdp.R

class TripRouteListActivity : BaseActivity<ActivityTripRouteListBinding>() {
    private var routeListAdapter: RouteListAdapter? = null


    override fun inflateBinding(): ActivityTripRouteListBinding {
        return ActivityTripRouteListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.rvRouteList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )
        binding.rvRouteList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        routeListAdapter = RouteListAdapter(
            this@TripRouteListActivity, Utils.getDummyArrayList(5)
        )
        binding.rvRouteList.adapter = routeListAdapter

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }


    }
}