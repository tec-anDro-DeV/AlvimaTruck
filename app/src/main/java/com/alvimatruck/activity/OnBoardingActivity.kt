package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.alvimatruck.R
import com.alvimatruck.adapter.SliderAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityOnBoardingBinding

class OnBoardingActivity : BaseActivity<ActivityOnBoardingBinding>() {

    private lateinit var sliderAdapter: SliderAdapter
    private var dots: Array<TextView?>? = null
    private lateinit var layouts: Array<Int>


    override fun inflateBinding(): ActivityOnBoardingBinding {
        return ActivityOnBoardingBinding.inflate(layoutInflater)
    }

    private val sliderChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageSelected(position: Int) {
            addBottomDots(position)

            if (position == layouts.size.minus(1)) {
                //   binding.nextBtn.visibility = View.GONE
                binding.dotsLayout.visibility = View.GONE
                binding.startBtn.visibility = View.VISIBLE
            } else {
                // binding.nextBtn.visibility = View.VISIBLE
                binding.dotsLayout.visibility = View.VISIBLE
                binding.startBtn.visibility = View.GONE
            }
        }

        override fun onPageScrollStateChanged(state: Int) {

        }

        override fun onPageScrolled(
            position: Int, positionOffset: Float, positionOffsetPixels: Int
        ) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layouts = arrayOf(
            R.layout.onboarding_slide,
            R.layout.onboarding_slide,
            R.layout.onboarding_slide,
            R.layout.onboarding_slide
        )

        sliderAdapter = SliderAdapter(this, layouts)

        dataSet()


        binding.skipBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.startBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }

    private fun dataSet() {
        /**
         * Adding bottom dots
         * */
        addBottomDots(0)

        binding.slider.apply {
            adapter = sliderAdapter
            addOnPageChangeListener(sliderChangeListener)
        }
    }

    private fun addBottomDots(currentPage: Int) {
        dots = arrayOfNulls(layouts.size)
        binding.dotsLayout.removeAllViews()

        for (i in dots!!.indices) {
            val imageView = ImageView(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            imageView.layoutParams = params

            if (i == currentPage) {
                imageView.setImageResource(R.drawable.dot_line)
            } else {
                imageView.setImageResource(R.drawable.dot_filled)
            }

            binding.dotsLayout.addView(imageView)
            dots!![i] = null // no need to track TextViews now
        }
    }


}