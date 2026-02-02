package com.alvimatruck.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.alvimatruck.R
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityFuelRefillRequestBinding
import com.alvimatruck.service.AlvimaTuckApplication
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.alvimatruck.utils.Utils.CAMERA_PERMISSION
import com.alvimatruck.utils.Utils.READ_EXTERNAL_STORAGE
import com.alvimatruck.utils.Utils.READ_MEDIA_IMAGES
import com.google.gson.JsonObject
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class FuelRefillRequestActivity : BaseActivity<ActivityFuelRefillRequestBinding>() {

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private var meterProofImageUri: Uri? = null
    private lateinit var cropLauncher: ActivityResultLauncher<Intent>


    override fun inflateBinding(): ActivityFuelRefillRequestBinding {
        return ActivityFuelRefillRequestBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndStartLocationService()

        setupLaunchers()


        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.tvCancel.setOnClickListener {
            handleBackPressed()
        }

        binding.rlChoosePhoto.setOnClickListener {
            openImageChooseDailog()
        }

        binding.tvSubmit.setOnClickListener {
            if (binding.etFuelAmount.text.toString().trim().isEmpty()) {
                Toast.makeText(
                    this, getString(R.string.please_enter_fuel_amount), Toast.LENGTH_SHORT
                ).show()
            } else if (meterProofImageUri == null) {
                Toast.makeText(
                    this, getString(R.string.please_upload_meter_receipt_photo), Toast.LENGTH_SHORT
                ).show()
            } else {
                apiFuelRequest()
            }
        }


        binding.btnDeleteMeterProof.setOnClickListener {
            binding.ivIDProof.setImageURI(null) // Clear the ImageView
            meterProofImageUri = null // ✅ Reset the URI
            binding.rlMeterPhoto.visibility = View.GONE
            binding.rlChoosePhoto.visibility = View.VISIBLE
        }


        binding.ivIDProof.setOnClickListener {
            startActivity(
                Intent(
                    this, FullImageActivity::class.java
                ).putExtra(Constants.ImageUri, meterProofImageUri.toString())
            )
        }

    }

    private fun apiFuelRequest() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@FuelRefillRequestActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.fuelRequest(
                "0".toRequestBody("text/plain".toMediaType()),
                binding.etFuelAmount.text.toString().trim()
                    .toRequestBody("text/plain".toMediaType()),
                AlvimaTuckApplication.latitude.toString().toRequestBody("text/plain".toMediaType()),
                AlvimaTuckApplication.longitude.toString()
                    .toRequestBody("text/plain".toMediaType()),
                Utils.createFilePart("FuelRefillMeter", meterProofImageUri, this)
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@FuelRefillRequestActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@FuelRefillRequestActivity,
                                response.body()!!.get("message").toString().replace('"', ' ')
                                    .trim(),
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent()
                            setResult(RESULT_OK, intent)
                            finish()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@FuelRefillRequestActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@FuelRefillRequestActivity,
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

    private fun openImageChooseDailog() {
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_image_selection, null)

        val btnCamera = alertLayout.findViewById<LinearLayout>(R.id.llCamera)
        val btnGallery = alertLayout.findViewById<LinearLayout>(R.id.llGallery)

        val tvCancel = alertLayout.findViewById<TextView>(R.id.tvCancel)
        val tvContinue = alertLayout.findViewById<TextView>(R.id.tvContinue)

        val dialog = AlertDialog.Builder(this).setView(alertLayout).setCancelable(false).create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        var isCamera = false


        btnCamera.setOnClickListener {
            isCamera = true
            btnCamera.setBackgroundResource(R.drawable.otp_box_focus_bg)
            btnGallery.setBackgroundResource(R.drawable.border_bg)

        }
        btnGallery.setOnClickListener {
            isCamera = false
            btnCamera.setBackgroundResource(R.drawable.border_bg)
            btnGallery.setBackgroundResource(R.drawable.otp_box_focus_bg)

        }

        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        tvContinue.setOnClickListener {
            if (isCamera) {
                checkCameraPermissionAndOpenCamera()
            } else {
                checkGalleryPermissionAndOpenGallery()
            }
            dialog.dismiss()
        }

        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun setupLaunchers() {
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val currentUri = meterProofImageUri

                    currentUri?.let { uri ->
                        handleImageResult(uri)
                    }
                }
            }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        handleImageResult(uri)
                    }
                }
            }

        cropLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.let {
                        val resultUri = UCrop.getOutput(it)
                        resultUri?.let { finalUri ->
                            handleCroppedImage(finalUri)
                        }
                    }
                }
            }
    }

    private fun startCrop(sourceUri: Uri) {

        val destinationUri = Uri.fromFile(
            File(cacheDir, "crop_${System.currentTimeMillis()}.jpg")
        )


        val options = UCrop.Options()

        // 🔒 Disable free-hand resizing
        options.setFreeStyleCropEnabled(true)

        // 🔒 Hide aspect ratio options (so user cannot change)
        options.setShowCropGrid(true)
        options.setShowCropFrame(true)
        options.setHideBottomControls(true)

        // Apply different aspect ratios
        val cropIntent = UCrop.of(sourceUri, destinationUri).withAspectRatio(16f, 9f)
            .withMaxResultSize(1600, 1600).withOptions(options).getIntent(this)


        cropLauncher.launch(cropIntent)
    }

    private fun handleCroppedImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.Main) {
            // Show a loading indicator if you have one
            val compressedUri = withContext(Dispatchers.IO) {
                // This runs the compression on a background thread
                Utils.getCompressedUri(this@FuelRefillRequestActivity, uri)
            }

            meterProofImageUri = compressedUri
            binding.ivIDProof.setImageURI(meterProofImageUri)
            binding.rlMeterPhoto.visibility = View.VISIBLE
            binding.rlChoosePhoto.visibility = View.GONE

            // Hide loading indicator
        }
    }

    private fun handleImageResult(imageUri: Uri) {
        startCrop(imageUri)
    }

    private fun checkCameraPermissionAndOpenCamera() {
        val permissionsNeeded = mutableListOf<String>()

        // For Android 13+ (API 33+), no need for WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(
                this, CAMERA_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) permissionsNeeded.add(CAMERA_PERMISSION)

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                Constants.CameraPermissionCode
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val photoFile = File(externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
        val currentUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)

        meterProofImageUri = currentUri

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentUri)
        cameraLauncher.launch(intent)
    }

    private fun checkGalleryPermissionAndOpenGallery() {
        val permissionsNeeded = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(
                    this, READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) permissionsNeeded.add(READ_MEDIA_IMAGES)
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(
                    this, READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) permissionsNeeded.add(READ_EXTERNAL_STORAGE)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                Constants.GalleryPermissionCode
            )
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            when (requestCode) {
                Constants.CameraPermissionCode -> openCamera()
                Constants.GalleryPermissionCode -> openGallery()
            }
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

}