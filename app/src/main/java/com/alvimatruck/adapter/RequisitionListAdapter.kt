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
import com.alvimatruck.activity.EditStoreRequisitionActivity
import com.alvimatruck.databinding.SingleRequisitionItemBinding
import com.alvimatruck.model.responses.LocationDetail
import com.alvimatruck.model.responses.RequisitionDetail


class RequisitionListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<RequisitionDetail>,
    private val locationList: ArrayList<LocationDetail>?,
    private val onSelectionChanged: (Boolean) -> Unit
) : RecyclerView.Adapter<RequisitionListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleRequisitionItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.detail = list[position]

        val item = list[position]

        // 1. Remove listener to prevent unwanted triggering during scrolling/recycling
        holder.binding.chkShip.setOnCheckedChangeListener(null)

        // 2. Set current state
        holder.binding.chkShip.isChecked = item.isSelected

        // 3. Add Listener
        holder.binding.chkShip.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked

            // Check if ALL items are now selected
            val isAllSelected = list.all { it.isSelected }

            // Notify the activity to update the main "Select All" checkbox
            onSelectionChanged(isAllSelected)
        }

        holder.binding.ivEdit.setOnClickListener {
            mActivity.startActivity(Intent(mActivity, EditStoreRequisitionActivity::class.java))
        }

        holder.binding.tvFromLocation.text =
            locationList?.find { it.code == list[position].fromLocation }?.name ?: ""


        if (list[position].status == "Open" || list[position].status == "Rejected") {
            holder.binding.tvStatus.background = ContextCompat.getDrawable(
                mActivity, R.drawable.bg_status_red
            )
        } else {
            holder.binding.tvStatus.background = ContextCompat.getDrawable(
                mActivity, R.drawable.bg_status_red
            )
        }

        if (list[position].status != "Open") {
            holder.binding.ivEdit.visibility = View.GONE
        } else {
            holder.binding.ivEdit.visibility = View.VISIBLE
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

    class ViewHolder(var binding: SingleRequisitionItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}