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
import com.alvimatruck.databinding.SingleTransferRequestBinding
import com.alvimatruck.interfaces.DeleteTransferRequestListener
import com.alvimatruck.model.responses.SingleTransfer


class TransferRequestItemListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<SingleTransfer>,
    val deleteOrderListener: DeleteTransferRequestListener
) : RecyclerView.Adapter<TransferRequestItemListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleTransferRequestBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.detail = list[position]
        if (position == list.size - 1) holder.binding.divider.visibility = View.GONE
        else holder.binding.divider.visibility = View.VISIBLE

        holder.binding.tvDelete.setOnClickListener {


            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_alert_two_button, null)

            val tvTitle = alertLayout.findViewById<TextView>(R.id.tvTitle)
            val tvMessage = alertLayout.findViewById<TextView>(R.id.tvMessage)
            val btnNo = alertLayout.findViewById<TextView>(R.id.btnNo)
            val btnYes = alertLayout.findViewById<TextView>(R.id.btnYes)

            // Set content
            tvTitle.text = mActivity.getString(R.string.delete_request)
            tvMessage.text =
                mActivity.getString(R.string.are_you_sure_you_want_to_delete_this_request)
            btnNo.text = mActivity.getString(R.string.no)
            btnYes.text = mActivity.getString(R.string.yes)


            val dialog =
                AlertDialog.Builder(mActivity).setView(alertLayout).setCancelable(false).create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


            btnNo.setOnClickListener {
                dialog.dismiss()
            }
            btnYes.setOnClickListener {
                dialog.dismiss()
                deleteOrderListener.onDeleteRequest(list[position])
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


    class ViewHolder(var binding: SingleTransferRequestBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}