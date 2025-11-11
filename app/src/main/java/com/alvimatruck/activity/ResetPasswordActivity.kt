package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : BaseActivity<ActivityResetPasswordBinding>() {
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
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_password_sucessfull, null)


            val tvContinue = alertLayout.findViewById<TextView>(R.id.tvContinue)
            val tvMessage = alertLayout.findViewById<TextView>(R.id.tvMessage)

            tvMessage.text =
                "Your password has been updated securely. Use your new password to sign in to your account."


            val dialog = AlertDialog.Builder(this)
                .setView(alertLayout)
                .setCancelable(false)
                .create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background2)


            tvContinue.setOnClickListener {
                dialog.dismiss()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()

            }
            dialog.show()
            val width =
                (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }

    }
}