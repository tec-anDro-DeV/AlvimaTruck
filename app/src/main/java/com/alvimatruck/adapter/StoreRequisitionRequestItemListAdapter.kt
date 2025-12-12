package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.databinding.SingleStoreRequisitionItemBinding
import com.alvimatruck.interfaces.DeleteOrderListener


class StoreRequisitionRequestItemListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<String>,
    val deleteOrderListener: DeleteOrderListener
) : RecyclerView.Adapter<StoreRequisitionRequestItemListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleStoreRequisitionItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        if (position == list.size - 1)
            holder.binding.divider.visibility = View.GONE
        else
            holder.binding.divider.visibility = View.VISIBLE

        holder.binding.tvDelete.setOnClickListener {


            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_alert_two_button, null)

            val tvTitle = alertLayout.findViewById<TextView>(R.id.tvTitle)
            val tvMessage = alertLayout.findViewById<TextView>(R.id.tvMessage)
            val btnNo = alertLayout.findViewById<TextView>(R.id.btnNo)
            val btnYes = alertLayout.findViewById<TextView>(R.id.btnYes)

            // Set content
            tvTitle.text = "Delete Request?"
            tvMessage.text = "Are you sure you want to delete this request?"
            btnNo.text = "No"
            btnYes.text = "Yes"


            val dialog =
                AlertDialog.Builder(mActivity).setView(alertLayout).setCancelable(false).create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


            btnNo.setOnClickListener {
                dialog.dismiss()
            }
            btnYes.setOnClickListener {
                dialog.dismiss()
                deleteOrderListener.onDeleteOrder("")
            }

            dialog.show()
            val width =
                (mActivity.resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        }


    }

    override fun getItemCount(): Int {
        return list.size
    }


    class ViewHolder(var binding: SingleStoreRequisitionItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}