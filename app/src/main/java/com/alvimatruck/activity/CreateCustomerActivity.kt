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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.adapter.SingleItemSelectionAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityCreateCustomerBinding
import com.alvimatruck.model.responses.CityDetail
import com.alvimatruck.service.AlvimaTuckApplication
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.alvimatruck.utils.Utils.CAMERA_PERMISSION
import com.alvimatruck.utils.Utils.READ_EXTERNAL_STORAGE
import com.alvimatruck.utils.Utils.READ_MEDIA_IMAGES
import com.google.gson.Gson
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

class CreateCustomerActivity : BaseActivity<ActivityCreateCustomerBinding>() {
    var postItemList: ArrayList<String>? = ArrayList()
    var priceItemList: ArrayList<String>? = ArrayList()
    var cityList: ArrayList<String>? = ArrayList()
    var postalCodeList: ArrayList<CityDetail>? = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()
    var selectedPostGroup = ""
    var selectedPriceGroup = ""
    var selectedCity = ""

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>


    private var customerPhotoUri: Uri? = null
    private var idProofImageUri: Uri? = null

    private var isUploadingCustomerPhoto: Boolean = true

    private lateinit var cropLauncher: ActivityResultLauncher<Intent>


    override fun inflateBinding(): ActivityCreateCustomerBinding {
        return ActivityCreateCustomerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        checkAndStartLocationService()

        setupLaunchers()
        getPriceList()
        getCityList()

        postItemList!!.add("DOMESTIC")
        postItemList!!.add("Distributor")
        postItemList!!.add("Whole-seller")
        postItemList!!.add("Retailers")
        postItemList!!.add("ABEDELLA")
        postItemList!!.add("Foreign")

        binding.tvCustomerPriceGroup.setOnClickListener {
            dialogSingleSelection(
                priceItemList!!,
                "Choose Price Group",
                "Search Price Group",
                binding.tvCustomerPriceGroup
            )
        }

        binding.tvCustomerPostingGroup.setOnClickListener {
            dialogSingleSelection(
                postItemList!!,
                "Choose Posting Group",
                "Search Posting Group",
                binding.tvCustomerPostingGroup
            )
        }

        binding.tvCity.setOnClickListener {
            dialogSingleSelection(
                cityList!!,
                "Choose City",
                "Search City",
                binding.tvCity,
                binding.tvPostalCode
            )
        }

        binding.tvCancel.setOnClickListener {
            handleBackPressed()
        }

        binding.tvCreate.setOnClickListener {
            validationAndSubmit()
        }

        binding.rlChoosePhoto.setOnClickListener {
            isUploadingCustomerPhoto = true
            openImageChooseDailog()
        }

        binding.rlChooseID.setOnClickListener {
            isUploadingCustomerPhoto = false
            openImageChooseDailog()
        }

        binding.btnDeleteUser.setOnClickListener {
            binding.ivUser.setImageURI(null) // Clear the ImageView
            customerPhotoUri = null // ✅
            binding.rlUser.visibility = View.GONE
            binding.rlChoosePhoto.visibility = View.VISIBLE
        }

        binding.btnDeleteIDProof.setOnClickListener {
            binding.ivIDProof.setImageURI(null) // Clear the ImageView
            idProofImageUri = null // ✅ Reset the URI
            binding.rlIDProof.visibility = View.GONE
            binding.rlChooseID.visibility = View.VISIBLE
        }


    }

    private fun validationAndSubmit() {
        if (binding.etCustomerName.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter customer name", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.etContactName.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter contact name", Toast.LENGTH_SHORT).show()
            return
        } else if (customerPhotoUri == null) {
            Toast.makeText(this, "Please upload customer photo", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.etCustomerPhoneNumber.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter customer phone number", Toast.LENGTH_SHORT).show()
            return
        } else if (Utils.isValidEthiopiaMobile(
                binding.etCustomerPhoneNumber.text.toString().trim()
            )
        ) {
            Toast.makeText(this, "Please enter valid customer phone number", Toast.LENGTH_SHORT)
                .show()
            return
        } else if (binding.etTelephoneNumber.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter telephone number", Toast.LENGTH_SHORT).show()
            return
        } else if (Utils.isValidEthiopiaLocalNumber(
                binding.etTelephoneNumber.text.toString().trim()
            )
        ) {
            Toast.makeText(this, "Please enter valid telephone number", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.tvCity.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select city", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.tvPostalCode.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter postal code", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.etAddress.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter address", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.etTINNumber.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter TIN number", Toast.LENGTH_SHORT).show()
            return
        } else if (idProofImageUri == null) {
            Toast.makeText(this, "Please upload ID proof", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.tvCustomerPostingGroup.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select posting group", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.tvCustomerPriceGroup.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select price group", Toast.LENGTH_SHORT).show()
            return
        } else {
            createUserApiCall()
        }
    }

    private fun createUserApiCall() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@CreateCustomerActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.createCustomer(
                binding.etCustomerName.text.toString().toRequestBody("text/plain".toMediaType()),
                binding.etContactName.text.toString().toRequestBody("text/plain".toMediaType()),
                binding.etCustomerPhoneNumber.text.toString()
                    .toRequestBody("text/plain".toMediaType()),
                binding.etTelephoneNumber.text.toString().toRequestBody("text/plain".toMediaType()),
                binding.tvCity.text.toString().toRequestBody("text/plain".toMediaType()),
                binding.tvPostalCode.text.toString().toRequestBody("text/plain".toMediaType()),
                binding.etTINNumber.text.toString().toRequestBody("text/plain".toMediaType()),
                binding.etAddress.text.toString().toRequestBody("text/plain".toMediaType()),
                binding.tvCustomerPostingGroup.text.toString()
                    .toRequestBody("text/plain".toMediaType()),
                binding.tvCustomerPriceGroup.text.toString()
                    .toRequestBody("text/plain".toMediaType()),
                AlvimaTuckApplication.latitude.toString().toRequestBody("text/plain".toMediaType()),
                AlvimaTuckApplication.longitude.toString()
                    .toRequestBody("text/plain".toMediaType()),
                Utils.createFilePart("CustomerImage", customerPhotoUri, this),
                Utils.createFilePart("IdProof", idProofImageUri, this)
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@CreateCustomerActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@CreateCustomerActivity,
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
                            this@CreateCustomerActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@CreateCustomerActivity,
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

    private fun getCityList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@CreateCustomerActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.cityList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            if (response.body() != null && response.body()!!.has("data")) {
                                postalCodeList = response.body()!!.getAsJsonArray("data").map {
                                    Gson().fromJson(it, CityDetail::class.java)
                                } as ArrayList<CityDetail>
                                for (item in postalCodeList!!) {
                                    val code = item.city
                                    cityList!!.add(code)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@CreateCustomerActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@CreateCustomerActivity,
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


    private fun getPriceList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@CreateCustomerActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.priceGroupList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            if (response.body() != null && response.body()!!.has("data")) {
                                val dataArray = response.body()!!.getAsJsonArray("data")

                                for (item in dataArray) {
                                    val obj = item.asJsonObject
                                    val code = obj.get("code").asString
                                    priceItemList!!.add(code)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@CreateCustomerActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@CreateCustomerActivity,
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

        val dialog =
            AlertDialog.Builder(this).setView(alertLayout).setCancelable(false).create()
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

    private fun dialogSingleSelection(
        list: ArrayList<String>,
        title: String,
        hint: String,
        textView: TextView,
        textView2: TextView? = null
    ) {
        filterList!!.clear()
        filterList!!.addAll(list)
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_single_selection, null)
        val selectedGroup: String = when (textView) {
            binding.tvCustomerPriceGroup -> {
                selectedPriceGroup
            }

            binding.tvCity -> {
                selectedCity
            }

            else -> {
                selectedPostGroup
            }
        }
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
            when (textView) {
                binding.tvCustomerPriceGroup -> {
                    selectedPriceGroup = singleItemSelectionAdapter.selected
                }

                binding.tvCity -> {
                    selectedCity = singleItemSelectionAdapter.selected

                    for (item in postalCodeList!!) {
                        if (item.city == singleItemSelectionAdapter.selected) {
                            textView2?.text = item.code
                        }
                    }
                }

                else -> {
                    selectedPostGroup = singleItemSelectionAdapter.selected
                }
            }
            textView.text = singleItemSelectionAdapter.selected
            dialog.dismiss()
        }
    }

    private fun setupLaunchers() {
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val currentUri =
                        if (isUploadingCustomerPhoto) customerPhotoUri else idProofImageUri
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
        if (isUploadingCustomerPhoto) {
            // 🔒 Disable free-hand resizing
            options.setFreeStyleCropEnabled(false)
        } else {
            options.setFreeStyleCropEnabled(true)
        }

        // 🔒 Hide aspect ratio options (so user cannot change)
        options.setShowCropGrid(true)
        options.setShowCropFrame(true)
        options.setHideBottomControls(true)

        // Apply different aspect ratios
        val cropIntent = if (isUploadingCustomerPhoto) {

            // ⭐ CUSTOMER PHOTO = 1:1 fixed
            UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(1080, 1080)
                .withOptions(options)
                .getIntent(this)

        } else {

            // ⭐ ID PROOF = 16:6 fixed
            UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(16f, 9f)
                .withMaxResultSize(1600, 600)
                .withOptions(options)
                .getIntent(this)
        }

        cropLauncher.launch(cropIntent)
    }

    private fun handleCroppedImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.Main) {
            // Show a loading indicator if you have one
            val compressedUri = withContext(Dispatchers.IO) {
                // This runs the compression on a background thread
                Utils.getCompressedUri(this@CreateCustomerActivity, uri)
            }

            if (isUploadingCustomerPhoto) {
                customerPhotoUri = compressedUri
                binding.ivUser.setImageURI(customerPhotoUri)
                binding.rlUser.visibility = View.VISIBLE
                binding.rlChoosePhoto.visibility = View.GONE
            } else {
                idProofImageUri = compressedUri
                binding.ivIDProof.setImageURI(idProofImageUri)
                binding.rlIDProof.visibility = View.VISIBLE
                binding.rlChooseID.visibility = View.GONE
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
        val currentUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)
        if (isUploadingCustomerPhoto) {
            customerPhotoUri = currentUri
        } else {
            idProofImageUri = currentUri
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentUri)
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

}