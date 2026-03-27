package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.databinding.SingleCreditPaymentLogItemBinding
import com.alvimatruck.model.responses.CreditPaymentDetail


class CreditPaymentLogsListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<CreditPaymentDetail>,
) : RecyclerView.Adapter<CreditPaymentLogsListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleCreditPaymentLogItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.detail = list[position]
        holder.binding.executePendingBindings()

        when (list[position].paymentStatus) {
            "Rejected" -> {
                holder.binding.tvStatus.background = ContextCompat.getDrawable(
                    mActivity, R.drawable.bg_status_red
                )

            }

            "Paid" -> {
                holder.binding.tvStatus.background = ContextCompat.getDrawable(
                    mActivity, R.drawable.bg_status_green
                )
            }

            else -> {
                holder.binding.tvStatus.background = ContextCompat.getDrawable(
                    mActivity, R.drawable.bg_status_orange
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: SingleCreditPaymentLogItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}