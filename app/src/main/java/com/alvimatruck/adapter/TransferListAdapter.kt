package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.databinding.SingleTransferItemBinding
import com.alvimatruck.model.responses.LocationDetail
import com.alvimatruck.model.responses.TransferDetail


class TransferListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<TransferDetail>,
    private val locationList: ArrayList<LocationDetail>?,
    private val onSelectionChanged: (Boolean) -> Unit
) : RecyclerView.Adapter<TransferListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleTransferItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        holder.binding.detail = list[position]

        // 1. Remove listener to prevent unwanted triggering during scrolling/recycling
        holder.binding.chkShip.setOnCheckedChangeListener(null)

        // 2. Set current state
        holder.binding.chkShip.isChecked = list[position].isSelected

        // 3. Add Listener
        holder.binding.chkShip.setOnCheckedChangeListener { _, isChecked ->
            list[position].isSelected = isChecked

            // Check if ALL items are now selected
            val isAllSelected = list.all { it.isSelected }

            // Notify the activity to update the main "Select All" checkbox
            onSelectionChanged(isAllSelected)
        }
        holder.binding.tvToLocation.text =
            locationList?.find { it.code == list[position].transferToCode }?.name ?: ""


    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll(isSelected: Boolean) {
        list.forEach { it.isSelected = isSelected }
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: SingleTransferItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}