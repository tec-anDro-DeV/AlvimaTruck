package com.alvimatruck.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityImagePickerBinding
import java.io.File


class ImagePickerActivity : BaseActivity<ActivityImagePickerBinding>() {

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private var imageUri: Uri? = null

    companion object {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        const val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES
        const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    }


    override fun inflateBinding(): ActivityImagePickerBinding {
        return ActivityImagePickerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLaunchers()

        binding.btnCamera.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }

        binding.btnGallery.setOnClickListener {
            checkGalleryPermissionAndOpenGallery()
        }

    }

    // region === Setup Launchers ===
    private fun setupLaunchers() {
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    binding.imageView.setImageURI(imageUri)
                }
            }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val selectedImageUri = result.data?.data
                    imageUri = selectedImageUri
                    binding.imageView.setImageURI(selectedImageUri)
                }
            }
    }
    // endregion

    // region === Camera ===
    private fun checkCameraPermissionAndOpenCamera() {
        val permissionsNeeded = mutableListOf<String>()

        // For Android 13+ (API 33+), no need for WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(
                this,
                CAMERA_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        )
            permissionsNeeded.add(CAMERA_PERMISSION)

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), 101)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val photoFile = File(externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
        imageUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }
    // endregion

    // region === Gallery ===
    private fun checkGalleryPermissionAndOpenGallery() {
        val permissionsNeeded = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(
                    this,
                    READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            )
                permissionsNeeded.add(READ_MEDIA_IMAGES)
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            )
                permissionsNeeded.add(READ_EXTERNAL_STORAGE)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), 102)
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }
    // endregion

    // region === Permissions Callback ===
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            when (requestCode) {
                101 -> openCamera()
                102 -> openGallery()
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    // endregion
}