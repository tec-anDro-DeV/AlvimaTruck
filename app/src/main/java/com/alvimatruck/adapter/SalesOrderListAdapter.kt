package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.activity.SalesOrderDetailActivity
import com.alvimatruck.databinding.SingleOrderItemBinding
import com.alvimatruck.model.responses.OrderDetail
import com.alvimatruck.utils.Constants


class SalesOrderListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<OrderDetail>,
) : RecyclerView.Adapter<SalesOrderListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleOrderItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.detail = list[position]
        //   holder.binding.tvData.text = "Demo List Item " + (position + 1)

        holder.itemView.setOnClickListener {
            mActivity.startActivity(
                Intent(mActivity, SalesOrderDetailActivity::class.java)
                    .putExtra(Constants.OrderID, list[position].orderId)
            )
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: SingleOrderItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}