package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.databinding.SingleShipReceiveItemBinding


class TransferShipAndReceiveListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<String>,
) : RecyclerView.Adapter<TransferShipAndReceiveListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleShipReceiveItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {


    }

    override fun getItemCount(): Int {
        return list.size
    }


    class ViewHolder(var binding: SingleShipReceiveItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}