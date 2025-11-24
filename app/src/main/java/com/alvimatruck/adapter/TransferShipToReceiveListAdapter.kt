package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.databinding.SingleReceiveItemBinding
import com.alvimatruck.utils.TransferItem


class TransferShipToReceiveListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<TransferItem>,
    private val onSelectionChanged: (Boolean) -> Unit
) : RecyclerView.Adapter<TransferShipToReceiveListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleReceiveItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = list[position]

        // 1. Remove listener to prevent unwanted triggering during scrolling/recycling
        holder.binding.chkReceive.setOnCheckedChangeListener(null)

        // 2. Set current state
        holder.binding.chkReceive.isChecked = item.isSelected

        // 3. Add Listener
        holder.binding.chkReceive.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked

            // Check if ALL items are now selected
            val isAllSelected = list.all { it.isSelected }

            // Notify the activity to update the main "Select All" checkbox
            onSelectionChanged(isAllSelected)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll(isSelected: Boolean) {
        list.forEach { it.isSelected = isSelected }
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: SingleReceiveItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}