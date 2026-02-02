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
import com.alvimatruck.databinding.ActivityConfirmDeliveryBinding
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

class ConfirmDeliveryActivity : BaseActivity<ActivityConfirmDeliveryBinding>() {
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private var deliveryProofImageUri: Uri? = null
    private var signatureUri: Uri? = null

    private var orderId: String? = null
    private lateinit var cropLauncher: ActivityResultLauncher<Intent>


    override fun inflateBinding(): ActivityConfirmDeliveryBinding {
        return ActivityConfirmDeliveryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndStartLocationService()

        setupLaunchers()

        if (intent != null) {
            orderId = intent.getStringExtra(Constants.OrderID).toString()
        }


        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        binding.btnClearSignature.setOnClickListener {
            binding.signature.clear()
        }

        binding.rlChoosePhoto.setOnClickListener {
            openImageChooseDailog()
        }


        binding.btnDeleteDeliveryProof.setOnClickListener {
            binding.ivIDProof.setImageURI(null) // Clear the ImageView
            deliveryProofImageUri = null // ✅ Reset the URI
            binding.rlDeliveryPhoto.visibility = View.GONE
            binding.rlChoosePhoto.visibility = View.VISIBLE
        }


        binding.ivIDProof.setOnClickListener {
            startActivity(
                Intent(
                    this, FullImageActivity::class.java
                ).putExtra(Constants.ImageUri, deliveryProofImageUri.toString())
            )
        }

        binding.tvConfirm.setOnClickListener {
            if (deliveryProofImageUri == null) {
                Toast.makeText(this, getString(R.string.please_select_a_photo), Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.signature.isEmpty) {
                Toast.makeText(this, getString(R.string.please_enter_signature), Toast.LENGTH_SHORT)
                    .show()
            } else {
                signatureUri = getSignatureUri()
                confirmDeliveryAPI()
            }
        }

    }

    private fun confirmDeliveryAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@ConfirmDeliveryActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.driverTripConfirm(
                orderId.toString().toRequestBody("text/plain".toMediaType()),
                binding.etRemark.text.trim().toString().toRequestBody("text/plain".toMediaType()),
                (AlvimaTuckApplication.latitude.toString() + "," + AlvimaTuckApplication.longitude).toRequestBody(
                    "text/plain".toMediaType()
                ),
                Utils.createFilePart("SignatureImage", signatureUri, this),
                Utils.createFilePart("DeliveryPhoto", deliveryProofImageUri, this)
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@ConfirmDeliveryActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@ConfirmDeliveryActivity,
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
                            this@ConfirmDeliveryActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@ConfirmDeliveryActivity,
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

    private fun getSignatureUri(): Uri? {
        return try {
            // 1. Get Bitmap from the signature view
            val bitmap = binding.signature.getBitmap()

            // 2. Create a temporary file in the cache directory
            val signatureFile = File(cacheDir, "signature_${System.currentTimeMillis()}.png")

            // 3. Save the bitmap to the file
            signatureFile.outputStream().use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }

            // 4. Return the Uri from the file
            Uri.fromFile(signatureFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
                    val currentUri = deliveryProofImageUri

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
                Utils.getCompressedUri(this@ConfirmDeliveryActivity, uri)
            }

            deliveryProofImageUri = compressedUri
            binding.ivIDProof.setImageURI(deliveryProofImageUri)
            binding.rlDeliveryPhoto.visibility = View.VISIBLE
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

        deliveryProofImageUri = currentUri

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