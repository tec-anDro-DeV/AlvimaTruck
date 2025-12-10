package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.databinding.SinglePaymentLogItemBinding


class PaymentLogsListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<String>,
) : RecyclerView.Adapter<PaymentLogsListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SinglePaymentLogItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        //   holder.binding.detail = list[position]
        //   holder.binding.tvData.text = "Demo List Item " + (position + 1)

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: SinglePaymentLogItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}