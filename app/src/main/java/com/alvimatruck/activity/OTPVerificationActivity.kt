package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityOtpverificationBinding

class OTPVerificationActivity : BaseActivity<ActivityOtpverificationBinding>() {
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 120000 // 2 minutes


    override fun inflateBinding(): ActivityOtpverificationBinding {
        return ActivityOtpverificationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startTimer()
        setupOtpInputs()

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.tvResendCode.setOnClickListener {
            if (binding.tvResendCode.isEnabled) {
                resendCode()
            }
        }

        binding.tvVerify.setOnClickListener {
            val otp = binding.etOtp1.text.toString().trim() +
                    binding.etOtp2.text.toString().trim() +
                    binding.etOtp3.text.toString().trim() +
                    binding.etOtp4.text.toString().trim()
            if (otp.length == 4) {
                startActivity(Intent(this, ResetPasswordActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Please enter all 4 digits", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setupOtpInputs() {
        val otpFields = listOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4
        )

        otpFields.forEachIndexed { index, editText ->

            // Set default background for all boxes
            editText.setBackgroundResource(R.drawable.otp_box_bg)

            // Change background when focused or unfocused
            editText.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    v.setBackgroundResource(R.drawable.otp_box_focus_bg)
                } else {
                    v.setBackgroundResource(R.drawable.otp_box_bg)
                }
            }

            // Text change logic for auto move / back focus
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus()
                    } else if (s?.isEmpty() == true && index > 0) {
                        otpFields[index - 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        // Automatically set focus on the first box initially
        otpFields.first().requestFocus()
    }


    private fun startTimer() {
        binding.tvResendCode.isEnabled = false
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000) % 60
                val minutes = (millisUntilFinished / 1000) / 60
                val formattedTime = String.format("%02d:%02d", minutes, seconds)
                binding.tvResendCode.text = formattedTime
            }

            override fun onFinish() {
                binding.tvResendCode.isEnabled = true
                binding.tvResendCode.text = "Resend Code"
            }
        }.start()
    }

    private fun resendCode() {
        Toast.makeText(this, "OTP code resent", Toast.LENGTH_SHORT).show()
        timeLeftInMillis = 120000
        startTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}