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
import android.widget.CheckBox
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
import com.alvimatruck.databinding.ActivitySendDepositBinding
import com.alvimatruck.model.responses.CustomerDetail
import com.alvimatruck.model.responses.InvoiceDetail
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
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class SendDepositActivity : BaseActivity<ActivitySendDepositBinding>() {
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private var paymentProofImageUri: Uri? = null

    var customerList: ArrayList<CustomerDetail>? = ArrayList()

    var selectedItem = ""
    var selectedCustomer: CustomerDetail? = null
    var filterList: ArrayList<String>? = ArrayList()
    var itemList: ArrayList<String>? = ArrayList()
    var invoiceList: ArrayList<InvoiceDetail>? = ArrayList()
    var selectedInvoiceList: ArrayList<String>? = ArrayList()
    var total = 0.0

    private lateinit var cropLauncher: ActivityResultLauncher<Intent>
    override fun inflateBinding(): ActivitySendDepositBinding {
        return ActivitySendDepositBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndStartLocationService()

        setupLaunchers()




        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        binding.rlChoosePhoto.setOnClickListener {
            openImageChooseDailog()
        }


        binding.btnDeletePaymentProof.setOnClickListener {
            binding.ivPaymentProof.setImageURI(null) // Clear the ImageView
            paymentProofImageUri = null // ✅ Reset the URI
            binding.rlPaymentPhoto.visibility = View.GONE
            binding.rlChoosePhoto.visibility = View.VISIBLE
        }

        binding.tvCustomer.setOnClickListener {
            dialogSingleSelection()
        }

        binding.tvCancel.setOnClickListener {
            handleBackPressed()
        }

        binding.rgPaymentMode.setOnCheckedChangeListener { _, checkedId ->

            val isCash = checkedId == R.id.rbCash
            binding.etTransRefNo.visibility = if (isCash) View.GONE else View.VISIBLE
            binding.tvTransRefNoLabel.visibility = if (isCash) View.GONE else View.VISIBLE

            when (checkedId) {
                R.id.rbCheque -> {
                    binding.tvTransRefNoLabel.text = getString(R.string.cheque_no)
                    binding.etTransRefNo.hint = getString(R.string.enter_cheque_no)
                }

                R.id.rbOnline -> {
                    binding.tvTransRefNoLabel.text = getString(R.string.transaction_ref_no)
                    binding.etTransRefNo.hint = getString(R.string.enter_transaction_ref_no)
                }
            }
        }


        binding.tvSubmit.setOnClickListener {
            if (selectedCustomer == null) {
                Toast.makeText(this, getString(R.string.please_select_customer), Toast.LENGTH_SHORT)
                    .show()
            } else if (selectedInvoiceList!!.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_select_invoice), Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.rgPaymentMode.checkedRadioButtonId == -1) {
                Toast.makeText(
                    this, getString(R.string.please_select_payment_mode), Toast.LENGTH_SHORT
                ).show()
            } else if (binding.rgPaymentMode.checkedRadioButtonId != R.id.rbCash && binding.etTransRefNo.text.toString()
                    .trim().isEmpty()
            ) {
                Toast.makeText(
                    this, getString(R.string.please_enter_transaction_ref_no), Toast.LENGTH_SHORT
                ).show()
            } else if (paymentProofImageUri == null) {
                Toast.makeText(
                    this, getString(R.string.please_upload_payment_proof), Toast.LENGTH_SHORT
                ).show()
            } else {
                paymentAPI()
            }
        }

        customerListAPI()

        binding.ivPaymentProof.setOnClickListener {
            startActivity(
                Intent(
                    this, FullImageActivity::class.java
                ).putExtra(Constants.ImageUri, paymentProofImageUri.toString())
            )
        }

        //  updateTotal()

    }

    private fun paymentAPI() {
        if (Utils.isOnline(this)) {
            val invoiceBodyList = ArrayList<RequestBody>()
            for (invoice in selectedInvoiceList!!) {
                invoiceBodyList.add(invoice.toRequestBody("text/plain".toMediaType()))
            }

            ProgressDialog.start(this@SendDepositActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.paymentCreate(
                selectedCustomer!!.bcCustomerNo.toRequestBody("text/plain".toMediaType()),
                binding.etTransRefNo.text.toString().toRequestBody("text/plain".toMediaType()),
                invoiceBodyList,
                Utils.createFilePart("imageFile", paymentProofImageUri, this),
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@SendDepositActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@SendDepositActivity,
                                response.body()!!.get("message").toString().replace('"', ' ')
                                    .trim(),
                                Toast.LENGTH_SHORT
                            ).show()
                            handleBackPressed()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@SendDepositActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@SendDepositActivity,
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

    private fun setupInvoiceCheckboxes() {
        binding.llInvoice.removeAllViews()

        invoiceList?.forEachIndexed { index, invoice ->

            val view = layoutInflater.inflate(R.layout.single_invoice, binding.llInvoice, false)

            val rb = view.findViewById<CheckBox>(R.id.cbInvoice)
            val root = view.findViewById<LinearLayout>(R.id.rootRow)
            val tvNo = view.findViewById<TextView>(R.id.tvInvoiceNo)
            val tvDate = view.findViewById<TextView>(R.id.tvInvoiceDate)
            val tvAmount = view.findViewById<TextView>(R.id.tvInvoiceAmount)
            val divider = view.findViewById<View>(R.id.viewDivider)

            tvNo.text = invoice.documentNo
            tvDate.text = "Date: ${invoice.getRequestDate() ?: "-"}"
            tvAmount.text = "ETB ${invoice.remainingAmount}"

            rb.tag = invoice
            rb.setOnClickListener {
                handleSingleSelection(index)
            }

            if (invoiceList!!.size - 1 == index) {
                divider.visibility = View.GONE
            } else {
                divider.visibility = View.VISIBLE
            }

            root.setOnClickListener {
                handleSingleSelection(index)
            }

            binding.llInvoice.addView(view)
        }
    }

    private fun handleSingleSelection(selectedIndex: Int) {
        for (i in 0 until binding.llInvoice.childCount) {
            val row = binding.llInvoice.getChildAt(i)
            val checkBox = row.findViewById<CheckBox>(R.id.cbInvoice)
            checkBox.isChecked = (i == selectedIndex)
        }
        updateTotal()
    }

    private fun updateTotal() {
        selectedInvoiceList!!.clear()
        // total = 0.0

        for (i in 0 until binding.llInvoice.childCount) {
            val row = binding.llInvoice.getChildAt(i)

            val checkBox = row.findViewById<CheckBox>(R.id.cbInvoice)

            if (checkBox.isChecked) {
                val invoice = checkBox.tag as InvoiceDetail
                selectedInvoiceList!!.add(invoice.documentNo)
                // total += invoice.remainingAmount
            }
        }

        //binding.tvtotal.text = "ETB $total"
    }

    private fun dialogSingleSelection() {
        filterList!!.clear()
        filterList!!.addAll(itemList!!)
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_single_selection, null)
        val selectedGroup: String = selectedItem

        val singleItemSelectionAdapter =
            SingleItemSelectionAdapter(this, filterList!!, selectedGroup)

        val lLayout = LinearLayoutManager(this)
        val rvBinList = alertLayout.findViewById<RecyclerView>(R.id.rvItemList)
        rvBinList.layoutManager = lLayout
        rvBinList.adapter = singleItemSelectionAdapter
        val etBinSearch = alertLayout.findViewById<EditText>(R.id.etItemSearch)
        etBinSearch.hint = getString(R.string.search_customer)



        etBinSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                //filter(s.toString())
                filterList!!.clear()
                if (s.toString().trim().isEmpty()) {
                    filterList!!.addAll(itemList!!)
                } else {
                    for (item in itemList) {
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
        tvTitle.text = getString(R.string.choose_customer)


        val alert = AlertDialog.Builder(this)
        alert.setView(alertLayout)
        alert.setCancelable(false)

        val dialog = alert.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        dialog.show()

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        tvCancel.setOnClickListener { _: View? -> dialog.dismiss() }
        tvConfirm.setOnClickListener { _: View? ->
            if (filterList!!.isNotEmpty()) {
                selectedItem = singleItemSelectionAdapter.selected
                for (item in customerList!!) {
                    val temp = item.searchName + " (" + item.bcCustomerNo + ")"
                    if (temp == singleItemSelectionAdapter.selected) {
                        selectedInvoiceList!!.clear()
                        total = 0.0
                        selectedCustomer = item
                        invoiceListAPI()
                    }
                }
                binding.tvCustomer.text = singleItemSelectionAdapter.selected
                dialog.dismiss()
            }
        }
    }

    private fun invoiceListAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@SendDepositActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.invoiceList(selectedCustomer!!.bcCustomerNo)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>, response: Response<JsonObject>
                    ) {
                        ProgressDialog.dismiss()
                        if (response.code() == 401) {
                            Utils.forceLogout(this@SendDepositActivity)  // show dialog before logout
                            return
                        }
                        if (response.isSuccessful) {
                            try {
                                Log.d("TAG", "onResponse: " + response.body().toString())
                                invoiceList = response.body()!!.getAsJsonArray("data").map {
                                    Gson().fromJson(it, InvoiceDetail::class.java)
                                } as ArrayList<InvoiceDetail>

                                if (invoiceList!!.isNotEmpty()) {
                                    binding.llInvoiceData.visibility = View.VISIBLE
                                    setupInvoiceCheckboxes()
                                } else {
                                    binding.llInvoiceData.visibility = View.GONE
                                    Toast.makeText(
                                        this@SendDepositActivity,
                                        "No invoice found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    selectedInvoiceList!!.clear()

                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                this@SendDepositActivity,
                                Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@SendDepositActivity,
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


    private fun customerListAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@SendDepositActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.customerList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>, response: Response<JsonObject>
                ) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@SendDepositActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())

                            customerList = response.body()!!.getAsJsonArray("items").map {
                                Gson().fromJson(it, CustomerDetail::class.java)
                            } as ArrayList<CustomerDetail>
                            if (customerList!!.isNotEmpty()) {
                                itemList!!.clear()
                                for (item in customerList!!) {
                                    val code = item.searchName + " (" + item.bcCustomerNo + ")"
                                    itemList!!.add(code)
                                }


                            } else {
                                Toast.makeText(
                                    this@SendDepositActivity,
                                    "No customer found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@SendDepositActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@SendDepositActivity,
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
                    val currentUri = paymentProofImageUri

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
                Utils.getCompressedUri(this@SendDepositActivity, uri)
            }

            paymentProofImageUri = compressedUri
            binding.ivPaymentProof.setImageURI(paymentProofImageUri)
            binding.rlPaymentPhoto.visibility = View.VISIBLE
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

        paymentProofImageUri = currentUri

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