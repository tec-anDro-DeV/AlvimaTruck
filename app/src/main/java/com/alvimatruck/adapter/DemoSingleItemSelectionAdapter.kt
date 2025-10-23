package com.alvimatruck.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R

class DemoSingleItemSelectionAdapter(
    private val context: Context,
    private val products: ArrayList<String>,
    private val selectedBin: String
) :
    RecyclerView.Adapter<DemoSingleItemSelectionAdapter.SingleViewHolder?>() {
    // if checkedPosition = -1, there is no default selection
    // if checkedPosition = 0, 1st item is selected by default
    private var checkedPosition = products.indexOf(selectedBin).takeIf { it >= 0 } ?: 0


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SingleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_item, viewGroup, false)
        return SingleViewHolder(view)
    }

    override fun onBindViewHolder(singleViewHolder: SingleViewHolder, position: Int) {
        singleViewHolder.bind(products[position])
    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class SingleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.textView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(product: String) {
            if (checkedPosition == -1) {
                //     imageView.setVisibility(View.GONE);
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.outline_circle
                    )
                )
            } else {
                if (checkedPosition == bindingAdapterPosition) {
                    //   imageView.setVisibility(View.VISIBLE);
                    imageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.select_circle
                        )
                    )
                } else {
                    //   imageView.setVisibility(View.GONE);
                    imageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.outline_circle
                        )
                    )
                }
            }
            textView.text = product.toString()

            itemView.setOnClickListener { view: View? ->
                //imageView.setVisibility(View.VISIBLE);
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.select_circle
                    )
                )
                if (checkedPosition != bindingAdapterPosition) {
                    notifyItemChanged(checkedPosition)
                    checkedPosition = bindingAdapterPosition
                }
            }
        }
    }

    val selected: String?
        get() {
            if (checkedPosition != -1) {
                return products[checkedPosition]
            }
            return null
        }
}