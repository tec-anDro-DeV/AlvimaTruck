package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.activity.DeliveryOrderDetailActivity
import com.alvimatruck.activity.MapRouteActivity
import com.alvimatruck.databinding.SingleDeliveryItemBinding
import com.alvimatruck.model.responses.DeliveryTripDetail
import com.alvimatruck.utils.Constants
import com.google.gson.Gson


class DeliveryListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<DeliveryTripDetail>,
) : RecyclerView.Adapter<DeliveryListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleDeliveryItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.detail = list[position]
        holder.itemView.setOnClickListener {
            mActivity.startActivity(
                Intent(mActivity, DeliveryOrderDetailActivity::class.java).putExtra(
                    Constants.DeliveryDetail,
                    Gson().toJson(list[position])
                )
            )
        }
        holder.binding.tvViewMap.setOnClickListener {
            mActivity.startActivity(
                Intent(mActivity, MapRouteActivity::class.java).putExtra(
                    Constants.LATITUDE, list[position].latitude
                ).putExtra(Constants.LONGITUDE, list[position].longitude)
                    .putExtra(Constants.CustomerDetail, list[position].sellToCustomerName)
            )
        }
        holder.binding.llItemList.removeAllViews()

        for (i in list[position].postedSalesShipmentLines.indices) {

            val item = list[position].postedSalesShipmentLines[i]

            // ✅ Inflate item_product.xml
            val productView = LayoutInflater.from(mActivity)
                .inflate(R.layout.item_product, holder.binding.llItemList, false)

            // ✅ Find Views
            val tvProductName = productView.findViewById<TextView>(R.id.tvProductName)
            val tvProductDetails = productView.findViewById<TextView>(R.id.tvProductDetails)
            val dividerLine = productView.findViewById<View>(R.id.dividerLine)

            // ✅ Set Dynamic Data
            tvProductName.text = "${item.description}"
            tvProductDetails.text =
                "Qty: ${item.quantity} ${item.unitOfMeasure} • SKU: ${item.no}"

            // ✅ Hide Divider for Last Item
            if (i == list[position].postedSalesShipmentLines.size - 1) {
                dividerLine.visibility = View.GONE
            }

            // ✅ Add Inflated View into LinearLayout
            holder.binding.llItemList.addView(productView)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: SingleDeliveryItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}