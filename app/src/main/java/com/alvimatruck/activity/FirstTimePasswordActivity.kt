package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.alvimatruck.R
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityResetPasswordBinding
import com.alvimatruck.model.request.ResetPasswordRequest
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FirstTimePasswordActivity : BaseActivity<ActivityResetPasswordBinding>() {
    override fun inflateBinding(): ActivityResetPasswordBinding {
        return ActivityResetPasswordBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.etOldPassword.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.etOldPassword.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (binding.etOldPassword.right - drawableEnd.bounds.width() - binding.etOldPassword.paddingEnd)) {

                    // ✅ Prevent EditText from gaining focus / opening keyboard
                    binding.etOldPassword.clearFocus()
                    v.performClick() // for accessibility
                    v.cancelLongPress()
                    v.isFocusable = false
                    v.isFocusableInTouchMode = false

                    // 🔄 Toggle show/hide password
                    val isVisible =
                        binding.etOldPassword.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

                    if (isVisible) {
                        binding.etOldPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        binding.etOldPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.eye, 0
                        )
                    } else {
                        binding.etOldPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        binding.etOldPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.hide_eye, 0
                        )
                    }

                    // keep cursor at end
                    binding.etOldPassword.setSelection(binding.etOldPassword.text.length)

                    // restore focusable state
                    v.isFocusable = true
                    v.isFocusableInTouchMode = true

                    return@setOnTouchListener true
                }
            }
            false
        }


        binding.etNewPassword.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.etNewPassword.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (binding.etNewPassword.right - drawableEnd.bounds.width() - binding.etNewPassword.paddingEnd)) {

                    // ✅ Prevent EditText from gaining focus / opening keyboard
                    binding.etNewPassword.clearFocus()
                    v.performClick() // for accessibility
                    v.cancelLongPress()
                    v.isFocusable = false
                    v.isFocusableInTouchMode = false

                    // 🔄 Toggle show/hide password
                    val isVisible =
                        binding.etNewPassword.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

                    if (isVisible) {
                        binding.etNewPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        binding.etNewPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.eye, 0
                        )
                    } else {
                        binding.etNewPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        binding.etNewPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.hide_eye, 0
                        )
                    }

                    // keep cursor at end
                    binding.etNewPassword.setSelection(binding.etNewPassword.text.length)

                    // restore focusable state
                    v.isFocusable = true
                    v.isFocusableInTouchMode = true

                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.etConfirmPassword.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.etConfirmPassword.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (binding.etConfirmPassword.right - drawableEnd.bounds.width() - binding.etConfirmPassword.paddingEnd)) {

                    // ✅ Prevent EditText from gaining focus / opening keyboard
                    binding.etConfirmPassword.clearFocus()
                    v.performClick() // for accessibility
                    v.cancelLongPress()
                    v.isFocusable = false
                    v.isFocusableInTouchMode = false

                    // 🔄 Toggle show/hide password
                    val isVisible =
                        binding.etConfirmPassword.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

                    if (isVisible) {
                        binding.etConfirmPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        binding.etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.eye, 0
                        )
                    } else {
                        binding.etConfirmPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        binding.etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.hide_eye, 0
                        )
                    }

                    // keep cursor at end
                    binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)

                    // restore focusable state
                    v.isFocusable = true
                    v.isFocusableInTouchMode = true

                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.tvSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finishAffinity()
        }

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.tvResetPassword.setOnClickListener {
            if (binding.etOldPassword.text.trim().toString().isEmpty()) {
                Toast.makeText(
                    this, getString(R.string.please_enter_old_password), Toast.LENGTH_SHORT
                ).show()
            } else if (binding.etNewPassword.text.trim().toString().isEmpty()) {
                Toast.makeText(
                    this, getString(R.string.please_enter_new_password), Toast.LENGTH_SHORT
                ).show()
            } else if (binding.etConfirmPassword.text.trim().toString().isEmpty()) {
                Toast.makeText(
                    this, getString(R.string.please_enter_confirm_password), Toast.LENGTH_SHORT
                ).show()
            } else if (binding.etNewPassword.text.trim()
                    .toString() != binding.etConfirmPassword.text.trim().toString()
            ) {
                Toast.makeText(
                    this,
                    getString(R.string.new_password_and_confirm_password_does_not_match),
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                if (Utils.isOnline(this@FirstTimePasswordActivity)) {
                    resetNewPassword()
                } else {
                    Toast.makeText(
                        this@FirstTimePasswordActivity,
                        getString(R.string.please_check_your_internet_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                }


            }
        }

    }

    fun resetNewPassword() {
        ProgressDialog.start(this@FirstTimePasswordActivity)
        ApiClient.getRestClient(
            Constants.BASE_URL, SharedHelper.getKey(this@FirstTimePasswordActivity, Constants.Token)
        )!!.webservices.resetPassword(
            ResetPasswordRequest(
                binding.etOldPassword.text.toString().trim(),
                binding.etNewPassword.text.toString().trim()
            )
        ).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                ProgressDialog.dismiss()
                if (response.isSuccessful) {
                    try {
                        val inflater = layoutInflater
                        val alertLayout =
                            inflater.inflate(R.layout.dialog_password_sucessfull, null)


                        val tvContinue = alertLayout.findViewById<TextView>(R.id.tvContinue)


                        val dialog =
                            AlertDialog.Builder(this@FirstTimePasswordActivity).setView(alertLayout)
                                .setCancelable(false).create()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background2)


                        tvContinue.setOnClickListener {
                            dialog.dismiss()
                            startActivity(
                                Intent(
                                    this@FirstTimePasswordActivity, LoginActivity::class.java
                                )
                            )
                            finishAffinity()

                        }
                        dialog.show()
                        val width =
                            (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
                        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(
                        this@FirstTimePasswordActivity,
                        Utils.parseErrorMessage(response),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                Toast.makeText(
                    this@FirstTimePasswordActivity,
                    getString(R.string.api_fail_message),
                    Toast.LENGTH_SHORT
                ).show()
                ProgressDialog.dismiss()
            }
        })

    }
}