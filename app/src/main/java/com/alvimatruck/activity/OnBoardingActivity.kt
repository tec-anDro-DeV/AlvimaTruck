package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.viewpager.widget.ViewPager
import com.alvimatruck.R
import com.alvimatruck.adapter.SliderAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityOnBoardingBinding

class OnBoardingActivity : BaseActivity<ActivityOnBoardingBinding>() {

    private lateinit var sliderAdapter: SliderAdapter
    private var dots: Array<TextView?>? = null
    private lateinit var bgImages: Array<Int>

    private var titles = arrayOf(
        "Quality You Deliver",
        "Smart & Fast Logistics",
        "Accurate Orders, Happy Customers",
        "Track. Deliver. Succeed"
    )

    private var descriptions = arrayOf(
        "Carry Alvima’s high-standard pasta and flour safely to every customer with trust and pride.",
        "Enjoy seamless deliveries powered by fully automated systems and ERP-integrated routing.",
        "Ensure every delivery meets expectations, boosting customer satisfaction across Ethiopia.",
        "Real-time status updates help you complete your route efficiently and grow with Alvima."
    )


    override fun inflateBinding(): ActivityOnBoardingBinding {
        return ActivityOnBoardingBinding.inflate(layoutInflater)
    }

    private val sliderChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageSelected(position: Int) {
            addBottomDots(position)
            binding.ivOnboard.setImageDrawable(
                ContextCompat.getDrawable(
                    this@OnBoardingActivity,
                    bgImages[position]
                )
            )
            if (position == titles.size.minus(1)) {
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
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = true

        bgImages = arrayOf(
            R.drawable.onboard1,
            R.drawable.onboard2,
            R.drawable.onboard3,
            R.drawable.onboard4
        )

        sliderAdapter = SliderAdapter(this, titles, descriptions)

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
        dots = arrayOfNulls(titles.size)
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