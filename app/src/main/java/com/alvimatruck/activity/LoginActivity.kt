package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.adapter.DemoSingleItemSelectionAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityLoginBinding
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class LoginActivity : BaseActivity<ActivityLoginBinding>() {
    var itemList: ArrayList<String>? = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()
    override fun inflateBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        itemList!!.add("Item 1")
        itemList!!.add("Item 2")
        itemList!!.add("Item 3")
        itemList!!.add("Item 4")
        itemList!!.add("Item 5")
        itemList!!.add("Item 6")
        itemList!!.add("Item 7")
        itemList!!.add("Item 8")
        itemList!!.add("Item 9")

        binding.etPassword.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.etPassword.compoundDrawables[2]
                if (drawableEnd != null && event.rawX >= (binding.etPassword.right - drawableEnd.bounds.width() - binding.etPassword.paddingEnd)) {

                    // ✅ Prevent EditText from gaining focus / opening keyboard
                    binding.etPassword.clearFocus()
                    v.performClick() // for accessibility
                    v.cancelLongPress()
                    v.isFocusable = false
                    v.isFocusableInTouchMode = false

                    // 🔄 Toggle show/hide password
                    val isVisible = binding.etPassword.inputType ==
                            (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

                    if (isVisible) {
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

                    // keep cursor at end
                    binding.etPassword.setSelection(binding.etPassword.text.length)

                    // restore focusable state
                    v.isFocusable = true
                    v.isFocusableInTouchMode = true

                    return@setOnTouchListener true
                }
            }
            false
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

        binding.tvPersonName.setOnClickListener {
            filterList!!.clear()
            dialogSingleSelection(itemList!!, "Select Sales Person", "Search Sales Person")
        }

        binding.tvVanNumber.setOnClickListener {
            filterList!!.clear()
            dialogSingleSelection(itemList!!, "Select Van No.", "Search Van No.")
        }

    }


    private fun dialogSingleSelection(list: ArrayList<String>, title: String, hint: String) {
        filterList!!.addAll(list)
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_single_selection, null)

        var productSeletionsAdapter = DemoSingleItemSelectionAdapter(this, filterList!!, "")

        val lLayout = LinearLayoutManager(this)
        val rvBinList = alertLayout.findViewById<RecyclerView>(R.id.rvItemList)
        rvBinList.layoutManager = lLayout
        rvBinList.adapter = productSeletionsAdapter
        val etBinSearch = alertLayout.findViewById<EditText>(R.id.etItemSearch)
        etBinSearch.hint = hint



        etBinSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                //filter(s.toString())
                filterList!!.clear()
                if (s.toString().trim().isEmpty()) {
                    filterList!!.addAll(list)
                } else {
                    for (item in list) {
                        if (item.lowercase().contains(s.toString().lowercase())) {
                            filterList!!.add(item)
                        }
                    }
                }
                productSeletionsAdapter = DemoSingleItemSelectionAdapter(
                    this@LoginActivity, filterList!!, ""
                )
                rvBinList.adapter = productSeletionsAdapter
            }
        })

        val tvCancel = alertLayout.findViewById<TextView>(R.id.tvCancel2)
        val tvConfirm = alertLayout.findViewById<TextView>(R.id.tvConfirm2)
        val tvTitle = alertLayout.findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = title


        val alert = AlertDialog.Builder(this)
        alert.setView(alertLayout)
        alert.setCancelable(false)

        val dialog = alert.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        dialog.show()

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        tvCancel.setOnClickListener { view: View? -> dialog.dismiss() }
        tvConfirm.setOnClickListener { view: View? ->
            // binding.tvChangeBin.text = productSeletionsAdapter.selected
            dialog.dismiss()
        }
    }


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