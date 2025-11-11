package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
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
import com.alvimatruck.adapter.VanItemSelectionAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityLoginBinding
import com.alvimatruck.model.request.LoginRequest
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.model.responses.VanDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity<ActivityLoginBinding>() {
    var itemList: ArrayList<VanDetail>? = ArrayList()
    var filterList: ArrayList<VanDetail>? = ArrayList()
    var selectedVan: VanDetail? = null

    var userDetail: UserDetail? = null

    override fun inflateBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadCredentials()
        getVanList()
        // Set a listener to clear credentials if the user unchecks the box
        binding.chkRemember.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                // If user unchecks "Remember me", clear the saved credentials
                SharedHelper.putKey(this, Constants.Username, "")
                SharedHelper.putKey(this, Constants.Password, "")
                SharedHelper.putKey(this, Constants.VanNo, "")
                SharedHelper.putKey(this, Constants.RememberMe, false)
            }
        }


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


        binding.tvSignIn.setOnClickListener {

            if (binding.tvVanNumber.text.trim().toString().isEmpty()) {
                Toast.makeText(
                    this, "Please select van no.", Toast.LENGTH_SHORT
                ).show()
            } else if (binding.etPassword.text.trim().toString().isEmpty()) {
                Toast.makeText(
                    this, "Please enter password", Toast.LENGTH_SHORT
                ).show()
            } else {
                if (Utils.isOnline(this)) {
                    login(
                        binding.tvPersonName.text.toString(),
                        binding.etPassword.text.toString(),
                        binding.tvVanNumber.text.toString()
                    )
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.please_check_your_internet_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }



        checkForFingerprintLogin()

        binding.tvVanNumber.setOnClickListener {
            filterList!!.clear()
            dialogSingleSelection(itemList!!, "Select Van No.", "Search Van No.")
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, OTPVerificationActivity::class.java))
        }

    }

    private fun getVanList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@LoginActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.vanList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            itemList = response.body()!!.getAsJsonArray("data").map {
                                Gson().fromJson(it, VanDetail::class.java)
                            } as ArrayList<VanDetail>
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity, getString(R.string.api_fail_message), Toast.LENGTH_SHORT
                    ).show()
                    ProgressDialog.dismiss()
                }
            })
        } else {
            Toast.makeText(
                this,
                getString(R.string.please_check_your_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
        }


    }

    private fun loadCredentials() {
        val rememberMe = SharedHelper.getBoolKey(this, Constants.RememberMe)
        binding.chkRemember.isChecked = rememberMe
        if (rememberMe) {
            binding.tvPersonName.text = SharedHelper.getKey(this, Constants.Username)
            binding.etPassword.setText(SharedHelper.getKey(this, Constants.Password))
            binding.tvVanNumber.text = SharedHelper.getKey(this, Constants.VanNo)
        }
    }


    private fun dialogSingleSelection(list: ArrayList<VanDetail>, title: String, hint: String) {
        filterList!!.addAll(list)
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_single_selection, null)

        var vanItemSelectionAdapter = VanItemSelectionAdapter(this, filterList!!, selectedVan)

        val lLayout = LinearLayoutManager(this)
        val rvBinList = alertLayout.findViewById<RecyclerView>(R.id.rvItemList)
        rvBinList.layoutManager = lLayout
        rvBinList.adapter = vanItemSelectionAdapter
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
                        if (item.vanNo.lowercase().contains(s.toString().lowercase())) {
                            filterList!!.add(item)
                        }
                    }
                }
                vanItemSelectionAdapter.notifyDataSetChanged()
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
            selectedVan = vanItemSelectionAdapter.selected!!
            binding.tvVanNumber.text = vanItemSelectionAdapter.selected!!.vanNo
            binding.tvPersonName.text = vanItemSelectionAdapter.selected!!.salesPerson
            dialog.dismiss()
        }
    }


    private fun checkForFingerprintLogin() {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate =
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        val savedUsername = SharedHelper.getKey(this, Constants.Username)
        val savedPassword = SharedHelper.getKey(this, Constants.Password)
        val savedVanNumber = SharedHelper.getKey(this, Constants.VanNo)
        val fingerprintEnabled = SharedHelper.getBoolKey(this, Constants.FingerPrintEnabled)


        if (fingerprintEnabled && canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            showFingerprintPrompt(savedUsername, savedPassword, savedVanNumber)
        }
    }

    private fun showFingerprintPrompt(username: String, password: String, vannumber: String) {
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
                    if (Utils.isOnline(this@LoginActivity)) {
                        login(username, password, vannumber)
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            getString(R.string.please_check_your_internet_connection),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
                    Toast.makeText(applicationContext, "Authentication Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }


    fun login(username: String, password: String, vannumber: String) {

        ProgressDialog.start(this@LoginActivity)
        ApiClient.getRestClient(
            Constants.BASE_URL, ""
        )!!.webservices.login(
            LoginRequest(
                password, username, vannumber
            )
        ).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                ProgressDialog.dismiss()
                if (response.isSuccessful) {
                    try {
                        Toast.makeText(
                            this@LoginActivity,
                            response.body()!!.get("message").toString().replace('"', ' ')
                                .trim(),
                            Toast.LENGTH_SHORT
                        ).show()
                        userDetail = Gson().fromJson(
                            response.body()!!.getAsJsonObject("data"),
                            UserDetail::class.java
                        )
                        SharedHelper.putKey(
                            this@LoginActivity,
                            Constants.Token,
                            userDetail!!.token
                        )
                        if (userDetail!!.isDefaultPassword) {
                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    FirstTimePasswordActivity::class.java
                                )
                            )
                            finishAffinity()
                        } else {
                            SharedHelper.putKey(
                                this@LoginActivity,
                                Constants.UserDetail,
                                Gson().toJson(userDetail)
                            )


                            if (binding.chkRemember.isChecked) {
                                SharedHelper.putKey(
                                    this@LoginActivity,
                                    Constants.Username,
                                    username
                                )
                                SharedHelper.putKey(
                                    this@LoginActivity,
                                    Constants.Password,
                                    password
                                )
                                SharedHelper.putKey(this@LoginActivity, Constants.VanNo, vannumber)
                                SharedHelper.putKey(this@LoginActivity, Constants.RememberMe, true)
                            }

                            val biometricManager = BiometricManager.from(this@LoginActivity)
                            val canAuthenticate =
                                biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                            val fingerprintEnabled =
                                SharedHelper.getBoolKey(
                                    this@LoginActivity,
                                    Constants.FingerPrintEnabled
                                )
                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    HomeActivity::class.java
                                )
                            )
                            if (!fingerprintEnabled && canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                                // First-time login and hardware supports fingerprint → ask user
                                askEnableFingerprint(username, password, vannumber)
                            } else {
                                SharedHelper.putKey(
                                    this@LoginActivity,
                                    Constants.IS_LOGIN,
                                    true
                                )

                                finishAffinity()
                            }

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        Utils.parseErrorMessage(response),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                Toast.makeText(
                    this@LoginActivity, getString(R.string.api_fail_message), Toast.LENGTH_SHORT
                ).show()
                ProgressDialog.dismiss()
            }
        })
    }

    private fun askEnableFingerprint(username: String, password: String, vannumber: String) {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_alert_two_button, null)

            val tvTitle = alertLayout.findViewById<TextView>(R.id.tvTitle)
            val tvMessage = alertLayout.findViewById<TextView>(R.id.tvMessage)
            val btnNo = alertLayout.findViewById<TextView>(R.id.btnNo)
            val btnYes = alertLayout.findViewById<TextView>(R.id.btnYes)

            // Set content
            tvTitle.text = "Enable Fingerprint Login?"
            tvMessage.text = "Would you like to use your fingerprint for future logins?"
            btnNo.text = "No"
            btnYes.text = "Yes"


            val dialog = AlertDialog.Builder(this)
                .setView(alertLayout)
                .setCancelable(false)
                .create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


            btnNo.setOnClickListener {
                dialog.dismiss()
                startActivity(
                    Intent(
                        this@LoginActivity,
                        HomeActivity::class.java
                    )
                )
                finishAffinity()
            }
            btnYes.setOnClickListener {
                dialog.dismiss()
                SharedHelper.putKey(this, Constants.FingerPrintEnabled, true)
                SharedHelper.putKey(this, Constants.Username, username)
                SharedHelper.putKey(this, Constants.Password, password)
                SharedHelper.putKey(this, Constants.VanNo, vannumber)
                startActivity(
                    Intent(
                        this@LoginActivity,
                        HomeActivity::class.java
                    )
                )
                finishAffinity()
            }

            dialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }
}