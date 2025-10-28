package com.alvimatruck.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.alvimatruck.R

class SliderAdapter(
    private val context: Context,
    private val titles: Array<String>,
    private val descriptions: Array<String>
) :
    PagerAdapter() {

    private lateinit var layoutInflater: LayoutInflater

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = layoutInflater.inflate(R.layout.onboarding_slide, container, false)
        val titleTv = view.findViewById<TextView>(R.id.titleTv)
        val descTV = view.findViewById<TextView>(R.id.descTV)

        titleTv.text = titles[position]
        descTV.text = descriptions[position]
        container.addView(view)

        return view
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int = titles.size

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
    }
}