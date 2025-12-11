package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.databinding.SingleFleetLogItemBinding
import com.alvimatruck.model.responses.FleetLog


class RecentLogsListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<FleetLog>,
) : RecyclerView.Adapter<RecentLogsListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleFleetLogItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.detail = list[position]
        if (list[position].status == "Approved") {
            holder.binding.tvStatus.background =
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_status_green)
        } else {
            holder.binding.tvStatus.background =
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_status_red)
        }
        when (list[position].fleetType) {
            "FuleRefill" -> {
                holder.binding.rlVendor.visibility = View.GONE
                holder.binding.rlDescription.visibility = View.GONE
                holder.binding.rlCost.visibility = View.VISIBLE
                holder.binding.llType.visibility = View.GONE
            }

            "RepairLog" -> {
                holder.binding.rlVendor.visibility = View.VISIBLE
                holder.binding.rlDescription.visibility = View.GONE
                holder.binding.rlCost.visibility = View.VISIBLE
                holder.binding.llType.visibility = View.GONE
            }

            else -> {
                holder.binding.rlVendor.visibility = View.GONE
                holder.binding.rlDescription.visibility = View.VISIBLE
                holder.binding.rlCost.visibility = View.GONE
                holder.binding.llType.visibility = View.VISIBLE
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: SingleFleetLogItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}