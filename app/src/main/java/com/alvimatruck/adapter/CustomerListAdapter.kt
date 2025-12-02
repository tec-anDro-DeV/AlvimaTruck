package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.activity.MapRouteActivity
import com.alvimatruck.activity.ViewCustomerActivity
import com.alvimatruck.databinding.SingleCustomerItemBinding
import com.alvimatruck.model.responses.CustomerDetail
import com.alvimatruck.utils.Constants
import com.google.gson.Gson


class CustomerListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<CustomerDetail>,
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
            mActivity.startActivity(
                Intent(mActivity, ViewCustomerActivity::class.java)
                    .putExtra(Constants.CustomerDetail, Gson().toJson(list[position]))
            )
        }

        holder.binding.tvViewMap.setOnClickListener {
            mActivity.startActivity(
                Intent(mActivity, MapRouteActivity::class.java).putExtra(
                    Constants.LATITUDE, list[position].latitude
                ).putExtra(Constants.LONGITUDE, list[position].longitude)
                    .putExtra(Constants.CustomerDetail, list[position].searchName)
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: SingleCustomerItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}