package com.alvimatruck.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.adapter.ImagesListAdapter
import com.alvimatruck.adapter.SingleItemSelectionAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityIncidentReportingBinding
import com.alvimatruck.interfaces.DeletePhotoListener
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
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class IncidentReportingActivity : BaseActivity<ActivityIncidentReportingBinding>(),
    DeletePhotoListener {
    override fun inflateBinding(): ActivityIncidentReportingBinding {
        return ActivityIncidentReportingBinding.inflate(layoutInflater)
    }


    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private var proofImageUri: Uri? = null
    private lateinit var cropLauncher: ActivityResultLauncher<Intent>

    private var imagesListAdapter: ImagesListAdapter? = null
    private var listProofImageUri: ArrayList<Uri> = ArrayList()

    var selectedType = ""
    var typeList: ArrayList<String>? = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listProofImageUri.clear()

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        setupLaunchers()

        binding.tvCancel.setOnClickListener {
            handleBackPressed()
        }

        binding.rlChoosePhoto.setOnClickListener {
            if (listProofImageUri.size < 5) {
                openImageChooseDialog()
            }
        }

        binding.rvPhotos.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._7sdp).toInt(),
                EqualSpacingItemDecoration.GRID
            )
        )
        binding.rvPhotos.layoutManager = GridLayoutManager(this, 3)


        imagesListAdapter = ImagesListAdapter(
            this@IncidentReportingActivity, listProofImageUri, this
        )
        binding.rvPhotos.adapter = imagesListAdapter

        binding.tvIncidentType.setOnClickListener {
            typeList!!.clear()
            typeList!!.add("Accident / Collision")
            typeList!!.add("Vehicle Damage")
            typeList!!.add("Breakdown / Mechanical Issue")
            typeList!!.add("Tyre or Wheel Damage")
            typeList!!.add("Load Damage / Spillage")
            typeList!!.add("Fuel Issue")
            typeList!!.add("Delay / Route Issue")
            typeList!!.add("Other")

            dialogSingleSelection(
                typeList!!,
                getString(R.string.choose_incident_type),
                getString(R.string.search_incident_type),
                binding.tvIncidentType,
            )
        }

        binding.tvSubmit.setOnClickListener {
            if (binding.tvIncidentType.text.toString().trim().isEmpty()) {
                Toast.makeText(
                    this, getString(R.string.please_select_incident_type), Toast.LENGTH_SHORT
                ).show()
            } else if (binding.etDescription.text.toString().trim().isEmpty()) {
                Toast.makeText(
                    this, getString(R.string.please_enter_description), Toast.LENGTH_SHORT
                ).show()
            } else if (listProofImageUri.isEmpty()) {
                Toast.makeText(
                    this, getString(R.string.please_upload_incident_images), Toast.LENGTH_SHORT
                ).show()
            } else {
                apiIncidentRequest()
            }
        }
    }

    private fun apiIncidentRequest() {

        val partsList = ArrayList<MultipartBody.Part>()

        listProofImageUri.forEachIndexed { index, file ->
            val part = Utils.createFilePart("IncidentImage", listProofImageUri[index], this)
            partsList.add(part!!)
        }
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@IncidentReportingActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.incidentReportRequest(
                "2".toRequestBody("text/plain".toMediaType()),
                AlvimaTuckApplication.latitude.toString().toRequestBody("text/plain".toMediaType()),
                AlvimaTuckApplication.longitude.toString()
                    .toRequestBody("text/plain".toMediaType()),
                binding.tvIncidentType.text.toString().trim()
                    .toRequestBody("text/plain".toMediaType()),
                binding.etDescription.text.toString().trim()
                    .toRequestBody("text/plain".toMediaType()),
                partsList
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@IncidentReportingActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@IncidentReportingActivity,
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
                            this@IncidentReportingActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@IncidentReportingActivity,
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

    private fun dialogSingleSelection(
        list: ArrayList<String>, title: String, hint: String, textView: TextView
    ) {
        filterList!!.clear()
        filterList!!.addAll(list)
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_single_selection, null)
        val selectedGroup: String = selectedType
        val singleItemSelectionAdapter =
            SingleItemSelectionAdapter(this, filterList!!, selectedGroup)

        val lLayout = LinearLayoutManager(this)
        val rvBinList = alertLayout.findViewById<RecyclerView>(R.id.rvItemList)
        rvBinList.layoutManager = lLayout
        rvBinList.adapter = singleItemSelectionAdapter
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
                singleItemSelectionAdapter.notifyDataSetChanged()
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
            if (filterList!!.isNotEmpty()) {
                selectedType = singleItemSelectionAdapter.selected
                textView.text = singleItemSelectionAdapter.selected
                dialog.dismiss()
            }
        }
    }


    private fun openImageChooseDialog() {
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
                    val currentUri = proofImageUri

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
        options.setFreeStyleCropEnabled(false)

        // 🔒 Hide aspect ratio options (so user cannot change)
        options.setShowCropGrid(true)
        options.setShowCropFrame(true)
        options.setHideBottomControls(true)

        // Apply different aspect ratios
        val cropIntent = UCrop.of(sourceUri, destinationUri).withAspectRatio(1f, 1f)
            .withMaxResultSize(1200, 1200).withOptions(options).getIntent(this)


        cropLauncher.launch(cropIntent)
    }

    private fun handleCroppedImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.Main) {
            // Show a loading indicator if you have one
            val compressedUri = withContext(Dispatchers.IO) {
                // This runs the compression on a background thread
                Utils.getCompressedUri(this@IncidentReportingActivity, uri)
            }

            compressedUri.let {
                if (listProofImageUri.size >= 5) return@let
                listProofImageUri.add(it)
                // Optimize: Only notify the item that was added, not the whole list
                imagesListAdapter?.notifyItemInserted(listProofImageUri.size - 1)
                if (listProofImageUri.size == 5) {
                    binding.rlChoosePhoto.visibility = View.GONE
                } else {
                    binding.rlChoosePhoto.visibility = View.VISIBLE
                }
            }
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
                this, permissionsNeeded.toTypedArray(), Constants.CameraPermissionCode
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val photoFile = File(externalCacheDir, "photo_${System.currentTimeMillis()}.jpg")
        val currentUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)

        proofImageUri = currentUri

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
                this, permissionsNeeded.toTypedArray(), Constants.GalleryPermissionCode
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

    override fun onDeletePhoto(imageUri: Uri) {
        listProofImageUri.remove(imageUri)
        imagesListAdapter!!.notifyDataSetChanged()


        binding.rlChoosePhoto.visibility = View.VISIBLE


    }
}