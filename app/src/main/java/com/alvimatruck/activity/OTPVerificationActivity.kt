package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.alvimatruck.R
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityOtpverificationBinding
import com.alvimatruck.model.request.OTPRequest
import com.alvimatruck.model.request.OTPVerifyRequest
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.Utils
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OTPVerificationActivity : BaseActivity<ActivityOtpverificationBinding>() {
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 120000 // 2 minutes
    var vanNo: String = ""


    override fun inflateBinding(): ActivityOtpverificationBinding {
        return ActivityOtpverificationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) {
            vanNo = intent.getStringExtra(Constants.VanNo).toString()
            getOTP()
        }


        setupOtpInputs()

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.tvResendCode.setOnClickListener {
            if (binding.tvResendCode.isEnabled) {
                getOTP()
            }
        }

        binding.tvVerify.setOnClickListener {
            val otp = binding.etOtp1.text.toString().trim() + binding.etOtp2.text.toString()
                .trim() + binding.etOtp3.text.toString().trim() + binding.etOtp4.text.toString()
                .trim()
            if (otp.length == 4) {
                if (Utils.isOnline(this)) {
                    verifyOTP(otp)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.please_check_your_internet_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this, getString(R.string.please_enter_all_4_digits), Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun verifyOTP(otp: String) {
        ProgressDialog.start(this@OTPVerificationActivity)
        ApiClient.getRestClient(
            Constants.BASE_URL, ""
        )!!.webservices.otpVerify(
            OTPVerifyRequest(vanNo, otp)
        ).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                ProgressDialog.dismiss()
                if (response.isSuccessful) {
                    try {
                        Toast.makeText(
                            this@OTPVerificationActivity,
                            response.body()!!.get("message").toString().replace('"', ' ').trim(),
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(
                            Intent(
                                this@OTPVerificationActivity, ResetPasswordActivity::class.java
                            ).putExtra(Constants.VanNo, vanNo)
                        )
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(
                        this@OTPVerificationActivity,
                        Utils.parseErrorMessage(response),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                Toast.makeText(
                    this@OTPVerificationActivity,
                    getString(R.string.api_fail_message),
                    Toast.LENGTH_SHORT
                ).show()
                ProgressDialog.dismiss()
            }
        })

    }

    private fun getOTP() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@OTPVerificationActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.resendOtp(
                OTPRequest(vanNo)
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Toast.makeText(
                                this@OTPVerificationActivity,
                                response.body()!!.get("message").toString().replace('"', ' ')
                                    .trim(),
                                Toast.LENGTH_SHORT
                            ).show()
                            resendCode()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@OTPVerificationActivity,
                            Utils.parseErrorMessage(response),
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@OTPVerificationActivity,
                        getString(R.string.api_fail_message),
                        Toast.LENGTH_SHORT
                    ).show()
                    ProgressDialog.dismiss()
                }
            })
        } else {
            Toast.makeText(
                this, getString(R.string.please_check_your_internet_connection), Toast.LENGTH_SHORT
            ).show()
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
        timeLeftInMillis = 120000
        startTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}