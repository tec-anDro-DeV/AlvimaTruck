package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityStoreRequisitionDetailBinding
import com.alvimatruck.model.responses.LocationDetail
import com.alvimatruck.model.responses.TransferDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.SharedHelper
import com.google.gson.Gson
import com.google.gson.JsonParser

class TransferDetailActivity : BaseActivity<ActivityStoreRequisitionDetailBinding>() {

    var transferDetail: TransferDetail? = null
    var locationList: ArrayList<LocationDetail>? = ArrayList()


    override fun inflateBinding(): ActivityStoreRequisitionDetailBinding {
        return ActivityStoreRequisitionDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.tvTitle.text = getString(R.string.transfer_details)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }
        getToLocationList()
        if (intent != null) {
            transferDetail = Gson().fromJson(
                intent.getStringExtra(Constants.OrderDetail),
                TransferDetail::class.java
            )
            binding.tvTo.text =
                locationList?.find { it.code == transferDetail!!.transferToCode }?.name ?: ""
            binding.tvFrom.text = getString(R.string.van)

            binding.tvDateTime.text = transferDetail!!.getRequestDate()

            binding.llItemList.removeAllViews()

            for (i in 0 until transferDetail!!.transferOrderLines.size) {

                val item = transferDetail!!.transferOrderLines[i]

                // ✅ Inflate item_product.xml
                val productView = LayoutInflater.from(this)
                    .inflate(R.layout.item_product, binding.llItemList, false)

                // ✅ Find Views
                val tvProductName = productView.findViewById<TextView>(R.id.tvProductName)
                val tvProductDetails = productView.findViewById<TextView>(R.id.tvProductDetails)
                val dividerLine = productView.findViewById<View>(R.id.dividerLine)

                // ✅ Set Dynamic Data
                tvProductName.text = item.description
                tvProductDetails.text =
                    "Qty: ${item.quantity} ${item.unitOfMeasureCode} • SKU: ${item.itemNo}"

                // ✅ Hide Divider for Last Item
                if (i == transferDetail!!.transferOrderLines.size - 1) {
                    dividerLine.visibility = View.GONE
                }

                // ✅ Add Inflated View into LinearLayout
                binding.llItemList.addView(productView)
            }
        }


    }

    private fun getToLocationList() {
        val jsonString = SharedHelper.getKey(this, Constants.API_To_Location)
        if (jsonString.isNotEmpty()) {
            locationList =
                JsonParser.parseString(jsonString).asJsonObject.getAsJsonArray("data").map {
                    Gson().fromJson(it, LocationDetail::class.java)
                } as ArrayList<LocationDetail>
        }

    }

}