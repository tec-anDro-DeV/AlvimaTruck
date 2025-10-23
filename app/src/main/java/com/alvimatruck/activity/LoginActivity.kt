package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityLoginBinding
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class LoginActivity : BaseActivity<ActivityLoginBinding>() {
    override fun inflateBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.etPassword.setOnTouchListener { v, event ->
            if ((event.action == MotionEvent.ACTION_UP) && event.rawX >= (binding.etPassword.right - binding.etPassword.compoundDrawables[2].bounds.width())) {
                if (binding.etPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    binding.etPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.etPassword.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.eye,
                        0
                    )
                } else {
                    binding.etPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.etPassword.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.hide_eye,
                        0
                    )
                }
                binding.etPassword.setSelection(binding.etPassword.text.length)
                true
            } else false
        }

//        binding.etPassword.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                val password = s.toString()
//                binding.passwordStrengthBar.visibility =
//                    if (password.isEmpty()) View.GONE else View.VISIBLE
//                updatePasswordStrength(password, binding.passwordStrengthBar)
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })


        binding.tvSignIn.setOnClickListener {

            if (binding.tvPersonName.text.trim().toString().isEmpty()) {
                Toast.makeText(
                    this, "Please select Person", Toast.LENGTH_SHORT
                ).show()
            } else if (binding.etPassword.text.trim().toString().isEmpty()) {
                Toast.makeText(
                    this, "Please enter password", Toast.LENGTH_SHORT
                ).show()
            } else {
                if (Utils.isOnline(this)) {
//                    startActivity(
//                        Intent(
//                            this@LoginActivity, DemoActivity::class.java
//                        )
//                    )
                    login(binding.tvPersonName.text.toString(), binding.etPassword.text.toString())
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.please_check_your_internet_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            // startActivity(Intent(this, HomeActivity::class.java))
        }



        checkForFingerprintLogin()
    }

//    private fun updatePasswordStrength(password: String, bar: ProgressBar) {
//        var strength = 0
//
//        if (password.length >= 8) strength += 20
//        if (password.matches(Regex(".*[A-Z].*"))) strength += 20
//        if (password.matches(Regex(".*[a-z].*"))) strength += 20
//        if (password.matches(Regex(".*[0-9].*"))) strength += 20
//        if (password.matches(Regex(".*[!@#\$%^&*(),.?\":{}|<>].*"))) strength += 20
//
//        bar.progress = strength
//
//        val color = when {
//            strength <= 40 -> Color.RED
//            strength <= 60 -> Color.parseColor("#FFA500") // orange
//            strength <= 80 -> Color.YELLOW
//            else -> Color.GREEN
//        }
//        bar.progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
//    }


    private fun checkForFingerprintLogin() {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate =
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        val savedUsername = SharedHelper.getKey(this, "username")
        val savedPassword = SharedHelper.getKey(this, "password")
        val fingerprintEnabled = SharedHelper.getBoolKey(this, "fingerprint_enabled")


        if (fingerprintEnabled && canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            showFingerprintPrompt(savedUsername, savedPassword)
        }
    }

    private fun showFingerprintPrompt(username: String, password: String) {
        val executor = ContextCompat.getMainExecutor(this)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Authentication")
            .setSubtitle("Use your fingerprint to log in")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    login(username, password)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }


    fun login(username: String, password: String) {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate =
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        val fingerprintEnabled = SharedHelper.getBoolKey(this, "fingerprint_enabled")

        if (!fingerprintEnabled && canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            // First-time login and hardware supports fingerprint → ask user
            askEnableFingerprint(username, password)
        } else {
            // Either fingerprint already enabled or hardware not supported → go to DemoActivity
            startActivity(Intent(this@LoginActivity, DemoActivity::class.java))
            finishAffinity()
        }

//        ProgressDialog.start(this@LoginActivity)
//        ApiClient.getRestClient(
//            Constants.BASE_URL
//        )!!.webservices.login(
//            LoginRequest(
//                password, username
//            )
//        ).enqueue(object : Callback<JsonObject> {
//            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
//                ProgressDialog.dismiss()
//                if (response.isSuccessful) {
//                    try {
//                        if (response.body()!!.get("status").toString().replace('"', ' ').trim()
//                                .lowercase() != "true"
//                        ) {
//                            Toast.makeText(
//                                this@LoginActivity,
//                                response.body()!!.get("message").toString().replace('"', ' ')
//                                    .trim(),
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        } else {
//                            Toast.makeText(
//                                this@LoginActivity,
//                                response.body()!!.get("message").toString().replace('"', ' ')
//                                    .trim(),
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            SharedHelper.putKey(
//                                this@LoginActivity,
//                                Constants.UserName,
//                                response.body()!!.get("userID")
//                                    .toString().replace('"', ' ').trim()
//                            )
//
//                            SharedHelper.putKey(
//                                this@LoginActivity,
//                                Constants.UserEmail,
//                                response.body()!!.get("eMail")
//                                    .toString().replace('"', ' ').trim()
//                            )
//                            SharedHelper.putKey(
//                                this@LoginActivity, Constants.IS_LOGIN, true
//                            )
//
//                            startActivity(
//                                Intent(
//                                    this@LoginActivity, DemoActivity::class.java
//                                )
//                            )
//                            finishAffinity()
//                        }
//
//
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
//                Toast.makeText(
//                    this@LoginActivity, getString(R.string.api_fail_message), Toast.LENGTH_SHORT
//                ).show()
//                ProgressDialog.dismiss()
//            }
//        })
    }

    private fun askEnableFingerprint(username: String, password: String) {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            == BiometricManager.BIOMETRIC_SUCCESS
        ) {

            val alertDialog = BottomSheetDialog(this)
            alertDialog.setCancelable(false)
            alertDialog.setContentView(R.layout.bottom_alert_two_button)
            alertDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

            (alertDialog.findViewById<TextView>(R.id.tvTitle))!!.text =
                "Enable Fingerprint Login?"

            (alertDialog.findViewById<TextView>(R.id.tvMessage))!!.text =
                "Would you like to use your fingerprint for future logins?"
            val btnNo = alertDialog.findViewById<TextView>(R.id.btnNo)
            btnNo!!.text = "No"
            val btnYes = alertDialog.findViewById<TextView>(R.id.btnYes)
            btnYes!!.text = "Yes"
            btnNo.setOnClickListener {
                alertDialog.dismiss()
                startActivity(Intent(this@LoginActivity, DemoActivity::class.java))
                finishAffinity()
            }
            btnYes.setOnClickListener {
                alertDialog.dismiss()
                SharedHelper.putKey(this, "username", username)
                SharedHelper.putKey(this, "password", password)
                SharedHelper.putKey(this, "fingerprint_enabled", true)
                startActivity(Intent(this@LoginActivity, DemoActivity::class.java))
                finishAffinity()
            }
            alertDialog.show()
        }
    }
}