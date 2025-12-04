package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.activity.MapRouteActivity
import com.alvimatruck.databinding.SingleCustomerItemBinding
import com.alvimatruck.interfaces.CustomerClickListener
import com.alvimatruck.model.responses.CustomerDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.Utils


class CustomerListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<CustomerDetail>,
    private val customerClickListener: CustomerClickListener,
) : RecyclerView.Adapter<CustomerListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleCustomerItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.detail = list[position]
        //   holder.binding.tvData.text = "Demo List Item " + (position + 1)

        holder.itemView.setOnClickListener {
            customerClickListener.onCustomerClick(list[position])
        }

        holder.binding.tvViewMap.setOnClickListener {
            mActivity.startActivity(
                Intent(mActivity, MapRouteActivity::class.java).putExtra(
                    Constants.LATITUDE, list[position].latitude
                ).putExtra(Constants.LONGITUDE, list[position].longitude)
                    .putExtra(Constants.CustomerDetail, list[position].searchName)
            )
        }

        Utils.loadProfileWithPlaceholder(
            holder.itemView.context,
            holder.binding.ivCustomer,
            list[position].searchName,
            list[position].customerImage
        )

        if (list[position].status == "Pending") {
            holder.binding.tvStatus.visibility = View.VISIBLE
            holder.binding.tvStatus.text = list[position].status
            holder.binding.tvStatus.background =
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_status_red)
        } else {
            holder.binding.tvStatus.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: SingleCustomerItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}