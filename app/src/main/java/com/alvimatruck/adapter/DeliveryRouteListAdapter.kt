package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.databinding.SingleDeliveryRouteItemBinding
import com.alvimatruck.interfaces.DeliveryRouteClickListener


class DeliveryRouteListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<String>,
    private val routeClickListener: DeliveryRouteClickListener
) : RecyclerView.Adapter<DeliveryRouteListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleDeliveryRouteItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        //  holder.binding.detail = list[position]
//        //   holder.binding.tvData.text = "Demo List Item " + (position + 1)
//
//        when (list[position].status) {
//            "InProgress" -> {
//                holder.binding.tvStatus.setBackgroundResource(R.drawable.bg_status_orange)
//            }
//
//            "Completed" -> {
//                holder.binding.tvStatus.setBackgroundResource(R.drawable.bg_status_green)
//            }
//
//            else -> {
//                holder.binding.tvStatus.setBackgroundResource(R.drawable.bg_status_red)
//            }
//        }

        holder.itemView.setOnClickListener {
            routeClickListener.onRouteClick(list[position])
        }
//
//        holder.binding.tvViewMap.setOnClickListener {
//            mActivity.startActivity(
//                Intent(mActivity, RouteMapActivity::class.java).putExtra(
//                    Constants.RouteDetail, Gson().toJson(list[position])
//                )
//            )
//        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: SingleDeliveryRouteItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}