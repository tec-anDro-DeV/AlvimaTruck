package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.activity.ConfirmDeliveryActivity
import com.alvimatruck.databinding.SingleDeliveryItemBinding


class DeliveryListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<String>,
) : RecyclerView.Adapter<DeliveryListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleDeliveryItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.itemView.setOnClickListener {
            mActivity.startActivity(Intent(mActivity, ConfirmDeliveryActivity::class.java))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: SingleDeliveryItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}