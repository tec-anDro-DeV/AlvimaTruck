package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.databinding.SingleReceiveItemBinding
import com.alvimatruck.model.responses.TransferReciveDetail


class TransferShipToReceiveListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<TransferReciveDetail>,
    private val onSelectionChanged: (Boolean) -> Unit
) : RecyclerView.Adapter<TransferShipToReceiveListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleReceiveItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        holder.binding.detail = list[position]
        val item = list[position]

        // 1. Remove listener to prevent unwanted triggering during scrolling/recycling
        holder.binding.chkReceive.setOnCheckedChangeListener(null)

        // 2. Set current state
        holder.binding.chkReceive.isChecked = item.isSelected

        // 3. Add Listener
        holder.binding.chkReceive.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                // ✅ If selecting, quantity must be entered
                if (holder.binding.etQuantity.text.trim().toString().isEmpty()) {
                    Toast.makeText(mActivity, "Please enter quantity", Toast.LENGTH_SHORT).show()
                    holder.binding.chkReceive.isChecked = false
                    return@setOnCheckedChangeListener
                }

                // ✅ Add qty to model
                item.isSelected = true
                item.qtyToReceive = holder.binding.etQuantity.text.trim().toString().toInt()

            } else {

                // ✅ If deselecting → remove qty + clear EditText
                item.isSelected = false
                item.qtyToReceive = 0

                holder.binding.etQuantity.setText("")
            }

            onSelectionChanged(item.isSelected)
        }

        holder.binding.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) return
                if (s.toString()
                        .toInt() > (list[position].quantityShipped - list[position].quantityReceived)
                ) {
                    Toast.makeText(
                        mActivity,
                        "Only up to ${(list[position].quantityShipped - list[position].quantityReceived)} units are available for receiving",
                        Toast.LENGTH_SHORT
                    ).show()
                    s.delete(s.length - 1, s.length)
                }
            }
        })

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