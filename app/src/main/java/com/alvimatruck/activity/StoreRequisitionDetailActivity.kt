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
import com.alvimatruck.model.responses.RequisitionDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.SharedHelper
import com.google.gson.Gson
import com.google.gson.JsonParser

class StoreRequisitionDetailActivity : BaseActivity<ActivityStoreRequisitionDetailBinding>() {

    var requisitionDetail: RequisitionDetail? = null
    var locationList: ArrayList<LocationDetail>? = ArrayList()


    override fun inflateBinding(): ActivityStoreRequisitionDetailBinding {
        return ActivityStoreRequisitionDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }
        getToLocationList()
        if (intent != null) {
            requisitionDetail = Gson().fromJson(
                intent.getStringExtra(Constants.OrderDetail),
                RequisitionDetail::class.java
            )
            binding.tvTo.text = "VAN"
            binding.tvFrom.text =
                locationList?.find { it.code == requisitionDetail!!.fromLocation }?.name ?: ""
            binding.tvDateTime.text = requisitionDetail!!.getRequestedDate()

            binding.llItemList.removeAllViews()

            for (i in 0 until requisitionDetail!!.requisitionLines.size) {

                val item = requisitionDetail!!.requisitionLines[i]

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
                    "Qty: ${item.quantityRequested} ${item.unitOfMeasure} • SKU: ${item.no}"

                // ✅ Hide Divider for Last Item
                if (i == requisitionDetail!!.requisitionLines.size - 1) {
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