package com.alvimatruck.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.activity.FullImageActivity
import com.alvimatruck.databinding.SinglePhotoBinding
import com.alvimatruck.interfaces.DeletePhotoListener
import com.alvimatruck.utils.Constants


class ImagesListAdapter(
    private val mActivity: Activity,
    private val list: ArrayList<Uri>,
    private val deletePhotoListener: DeletePhotoListener
) : RecyclerView.Adapter<ImagesListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = mActivity.layoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SinglePhotoBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        Log.d("ImagesListAdapter", "position: $position")
        holder.binding.imgItem.setImageURI(list[position])

        holder.binding.btnDeletePhoto.setOnClickListener {
            deletePhotoListener.onDeletePhoto(list[position])
        }

        holder.binding.imgItem.setOnClickListener {
            mActivity.startActivity(
                Intent(
                    mActivity, FullImageActivity::class.java
                ).putExtra(Constants.ImageUri, list[position].toString())
            )
        }


    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: SinglePhotoBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}