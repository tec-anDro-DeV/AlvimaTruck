package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.alvimatruck.R
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityResetPasswordBinding
import com.alvimatruck.model.request.ChangePasswordRequest
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.Utils
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : BaseActivity<ActivityResetPasswordBinding>() {
    var vanNo: String = ""

    override fun inflateBinding(): ActivityResetPasswordBinding {
        return ActivityResetPasswordBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvOldPassword.visibility = View.GONE
        binding.etOldPassword.visibility = View.GONE

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
                    val isVisible = binding.etNewPassword.inputType ==
                            (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

                    if (isVisible) {
                        binding.etNewPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        binding.etNewPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.eye,
                            0
                        )
                    } else {
                        binding.etNewPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        binding.etNewPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.hide_eye,
                            0
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
                    val isVisible = binding.etConfirmPassword.inputType ==
                            (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

                    if (isVisible) {
                        binding.etConfirmPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        binding.etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.eye,
                            0
                        )
                    } else {
                        binding.etConfirmPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        binding.etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.hide_eye,
                            0
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

        if (intent != null) {
            vanNo = intent.getStringExtra(Constants.VanNo).toString()
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
            if (binding.etNewPassword.text.trim().toString().isEmpty()) {
                Toast.makeText(
                    this, "Please enter new password", Toast.LENGTH_SHORT
                ).show()
            } else if (binding.etConfirmPassword.text.trim().toString().isEmpty()) {
                Toast.makeText(
                    this, "Please enter confirm password", Toast.LENGTH_SHORT
                ).show()
            } else if (binding.etNewPassword.text.trim()
                    .toString() != binding.etConfirmPassword.text.trim().toString()
            ) {
                Toast.makeText(
                    this, "New password and confirm password does not match", Toast.LENGTH_SHORT
                ).show()
            } else {

                if (Utils.isOnline(this@ResetPasswordActivity)) {
                    resetNewPassword()
                } else {
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        getString(R.string.please_check_your_internet_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                }


            }
        }

    }

    fun resetNewPassword() {
        ProgressDialog.start(this@ResetPasswordActivity)
        ApiClient.getRestClient(
            Constants.BASE_URL, ""
        )!!.webservices.changePassword(
            ChangePasswordRequest(
                vanNo,
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
                        val tvMessage = alertLayout.findViewById<TextView>(R.id.tvMessage)

                        tvMessage.text =
                            "Your password has been updated securely. Use your new password to sign in to your account."


                        val dialog = AlertDialog.Builder(this@ResetPasswordActivity)
                            .setView(alertLayout)
                            .setCancelable(false)
                            .create()
                        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background2)


                        tvContinue.setOnClickListener {
                            dialog.dismiss()
                            startActivity(
                                Intent(
                                    this@ResetPasswordActivity,
                                    LoginActivity::class.java
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
                        this@ResetPasswordActivity,
                        Utils.parseErrorMessage(response),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                Toast.makeText(
                    this@ResetPasswordActivity,
                    getString(R.string.api_fail_message),
                    Toast.LENGTH_SHORT
                ).show()
                ProgressDialog.dismiss()
            }
        })

    }
}