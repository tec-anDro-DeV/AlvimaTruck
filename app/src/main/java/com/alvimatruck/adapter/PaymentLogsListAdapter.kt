package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.databinding.SinglePaymentLogItemBinding
import com.alvimatruck.model.responses.PaymentDetail


class PaymentLogsListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<PaymentDetail>,
) : RecyclerView.Adapter<PaymentLogsListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SinglePaymentLogItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.detail = list[position]
        //   holder.binding.tvData.text = "Demo List Item " + (position + 1)
        holder.binding.llInvoice.removeAllViews()
        val invoiceList = list[position].invoiceNumbers.split(",")
        invoiceList.forEachIndexed { index, invoice ->
            addInvoiceView(holder, invoice.trim(), index + 1)
        }
        holder.binding.executePendingBindings()

        if (list[position].status == "Pending") {
            holder.binding.tvStatus.background =
                ContextCompat.getDrawable(
                    mActivity, R.drawable.bg_status_red
                )

        } else {
            holder.binding.tvStatus.background =
                ContextCompat.getDrawable(
                    mActivity, R.drawable.bg_status_green
                )
        }
    }

    private fun addInvoiceView(holder: ViewHolder, invoice: String, number: Int) {
        // Inflate the dedicated layout for a single invoice item
        val invoiceView: View =
            layoutInflater.inflate(R.layout.single_invoice_item, holder.binding.llInvoice, false)

        // Find the TextViews within the inflated invoice view
        val tvInvoiceLabel = invoiceView.findViewById<TextView>(R.id.tvInvoiceLabel)
        val tvInvoiceDetail = invoiceView.findViewById<TextView>(R.id.tvInvoiceDetail)

        // Set the dynamic data
        tvInvoiceLabel.text = "Invoice #$number"
        tvInvoiceDetail.text = invoice

        // Add the newly created and populated view to the parent LinearLayout
        holder.binding.llInvoice.addView(invoiceView)
    }


    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: SinglePaymentLogItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}