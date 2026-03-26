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
import android.widget.CheckBox
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
import com.alvimatruck.R
import com.alvimatruck.adapter.ImagesListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityNewSendPaymentBinding
import com.alvimatruck.interfaces.DeletePhotoListener
import com.alvimatruck.model.responses.BankDetail
import com.alvimatruck.model.responses.InvoiceDetail
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.alvimatruck.utils.Utils.CAMERA_PERMISSION
import com.alvimatruck.utils.Utils.READ_EXTERNAL_STORAGE
import com.alvimatruck.utils.Utils.READ_MEDIA_IMAGES
import com.google.gson.Gson
import com.google.gson.JsonObject
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

class NewSendPaymentActivity : BaseActivity<ActivityNewSendPaymentBinding>(), DeletePhotoListener {
    var invoiceList: ArrayList<InvoiceDetail>? = ArrayList()
    var selectedInvoiceList: String = ""

    var bankList: ArrayList<BankDetail>? = ArrayList()
    var selectedBankList: String = ""
    var selectedBankNoList: String = ""

    var maxPhotoLimit = 0


    var total = 0.0
    var userDetail: UserDetail? = null

    private var proofImageUri: Uri? = null

    private var listProofImageUri: ArrayList<Uri> = ArrayList()

    private var imagesListAdapter: ImagesListAdapter? = null

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun inflateBinding(): ActivityNewSendPaymentBinding {
        return ActivityNewSendPaymentBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userDetail =
            Gson().fromJson(SharedHelper.getKey(this, Constants.UserDetail), UserDetail::class.java)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }
        setupLaunchers()

        binding.tvBatchName.text = userDetail?.salesPersonCode
        binding.tvCashonHandNo.text = userDetail?.cashOnHand
        invoiceListAPI()
        bankListAPI()

        binding.rlChoosePhoto.setOnClickListener {
            if (listProofImageUri.size < maxPhotoLimit) {
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
            this@NewSendPaymentActivity, listProofImageUri, this
        )
        binding.rvPhotos.adapter = imagesListAdapter

        binding.tvInvoice.setOnClickListener {
            if (invoiceList!!.isNotEmpty()) {
                dialogInvoiceSelection()
            } else {
                Toast.makeText(this, "No invoices found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvBank.setOnClickListener {
            if (bankList!!.isNotEmpty()) {
                dialogBankSelection()
            } else {
                Toast.makeText(this, "No banks found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvSubmit.setOnClickListener {
            val transRefText = binding.etTransRefNo.text.toString().trim()
            val selectedBanksCount =
                if (selectedBankNoList.isEmpty()) 0 else selectedBankNoList.split(",").size

            val refNumbers = transRefText.split(",").filter { it.trim().isNotEmpty() }
            val enteredRefsCount = refNumbers.size

            if (selectedInvoiceList.isEmpty()) {
                Toast.makeText(this, "Please select invoice", Toast.LENGTH_SHORT).show()
            } else if (selectedBankNoList.isEmpty()) {
                Toast.makeText(this, "Please select bank", Toast.LENGTH_SHORT).show()
            } else if (transRefText.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter transaction reference number(s)",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (enteredRefsCount != selectedBanksCount) {
                // Validation: Check if count matches
                Toast.makeText(
                    this,
                    "Transaction reference number count does not match selected banks",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Success: Proceed to API Call
                sendPaymentAPI()
            }
        }
    }

    private fun sendPaymentAPI() {
        if (Utils.isOnline(this)) {

            val partsList = ArrayList<MultipartBody.Part>()

            listProofImageUri.forEachIndexed { index, _ ->
                val part = Utils.createFilePart("imageFile", listProofImageUri[index], this)
                partsList.add(part!!)
            }
            ProgressDialog.start(this@NewSendPaymentActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.paymentCreate(
                binding.tvInvoice.text.toString().toRequestBody("text/plain".toMediaType()),
                binding.tvBatchName.text.toString().toRequestBody("text/plain".toMediaType()),
                binding.etTransRefNo.text.toString().toRequestBody("text/plain".toMediaType()),
                selectedBankNoList.toRequestBody("text/plain".toMediaType()),
                binding.tvtotal.text.toString().replace("ETB", "")
                    .toRequestBody("text/plain".toMediaType()),
                binding.tvCashonHandNo.text.toString().toRequestBody("text/plain".toMediaType()),
                partsList
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401 || response.code() == 402) {
                        Utils.forceLogout(
                            this@NewSendPaymentActivity,
                            response.code()
                        )  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@NewSendPaymentActivity,
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
                            this@NewSendPaymentActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@NewSendPaymentActivity,
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


    private fun dialogBankSelection() {
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_bank_selection, null)


        val tvCancel = alertLayout.findViewById<TextView>(R.id.tvCancel2)
        val tvConfirm = alertLayout.findViewById<TextView>(R.id.tvConfirm2)
        val llBank = alertLayout.findViewById<LinearLayout>(R.id.llBank)

        llBank.removeAllViews()
        val currentSelectedIds = selectedBankNoList.split(",").toSet()

        bankList?.forEachIndexed { index, bank ->

            val view = layoutInflater.inflate(R.layout.single_bank, llBank, false)

            val cb = view.findViewById<CheckBox>(R.id.cbBank)
            val root = view.findViewById<LinearLayout>(R.id.rootRow)
            val tvName = view.findViewById<TextView>(R.id.tvBankName)
            val tvAccountNo = view.findViewById<TextView>(R.id.tvBankAccountNo)
            val divider = view.findViewById<View>(R.id.viewDivider)
            tvName.text = bank.name
            tvAccountNo.text = bank.no

            cb.isChecked = currentSelectedIds.contains(bank.no)

            cb.tag = bank

            if (bankList!!.size - 1 == index) {
                divider.visibility = View.GONE
            } else {
                divider.visibility = View.VISIBLE
            }

            root.setOnClickListener {
                cb.isChecked = !cb.isChecked
            }

            llBank.addView(view)
        }


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
            selectedBankNoList = ""
            selectedBankList = ""
            maxPhotoLimit = 0
            for (i in 0 until llBank.childCount) {
                val row = llBank.getChildAt(i)

                val checkBox = row.findViewById<CheckBox>(R.id.cbBank)

                if (checkBox.isChecked) {
                    maxPhotoLimit += 1
                    val bank = checkBox.tag as BankDetail
                    if (selectedBankNoList.isEmpty()) {
                        selectedBankNoList = bank.no
                        selectedBankList = bank.name
                        binding.tvMaxPhoto.visibility = View.GONE
                    } else {
                        binding.tvMaxPhoto.visibility = View.VISIBLE
                        selectedBankNoList += ",${bank.no}"
                        selectedBankList += ",${bank.name}"
                    }
                }
                binding.tvMaxPhoto.text = "Max $maxPhotoLimit photos"
            }
            binding.tvBank.text = selectedBankList.ifEmpty { "" }

            dialog.dismiss()
        }
    }

    private fun dialogInvoiceSelection() {
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_invoice_selection, null)


        val tvCancel = alertLayout.findViewById<TextView>(R.id.tvCancel2)
        val tvConfirm = alertLayout.findViewById<TextView>(R.id.tvConfirm2)
        val llInvoice1 = alertLayout.findViewById<LinearLayout>(R.id.llInvoice)

        llInvoice1.removeAllViews()
        val currentSelectedIds = selectedInvoiceList.split(",").toSet()

        invoiceList?.forEachIndexed { index, invoice ->

            val view = layoutInflater.inflate(R.layout.single_invoice, llInvoice1, false)

            val cb = view.findViewById<CheckBox>(R.id.cbInvoice)
            val root = view.findViewById<LinearLayout>(R.id.rootRow)
            val tvNo = view.findViewById<TextView>(R.id.tvInvoiceNo)
            //val tvDate = view.findViewById<TextView>(R.id.tvInvoiceDate)
            val tvAmount = view.findViewById<TextView>(R.id.tvInvoiceAmount)
            val divider = view.findViewById<View>(R.id.viewDivider)

            tvNo.text = invoice.documentNo
            //   tvDate.text = "Date: ${invoice.getRequestDate() ?: "-"}"
            tvAmount.text = "ETB ${invoice.amount}"

            cb.isChecked = currentSelectedIds.contains(invoice.documentNo)

            cb.tag = invoice

            if (invoiceList!!.size - 1 == index) {
                divider.visibility = View.GONE
            } else {
                divider.visibility = View.VISIBLE
            }

            root.setOnClickListener {
                cb.isChecked = !cb.isChecked
            }

            llInvoice1.addView(view)
        }


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
            selectedInvoiceList = ""
            total = 0.0

            for (i in 0 until llInvoice1.childCount) {
                val row = llInvoice1.getChildAt(i)

                val checkBox = row.findViewById<CheckBox>(R.id.cbInvoice)

                if (checkBox.isChecked) {
                    val invoice = checkBox.tag as InvoiceDetail
                    if (selectedInvoiceList.isEmpty()) {
                        selectedInvoiceList = invoice.documentNo
                    } else {
                        selectedInvoiceList += ",${invoice.documentNo}"
                    }
                    total += invoice.amount
                }
            }

            binding.tvtotal.text = "ETB $total"
            binding.tvInvoice.text =
                selectedInvoiceList.ifEmpty { "" }
            dialog.dismiss()
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

//        cropLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == RESULT_OK) {
//                    result.data?.let {
//                        val resultUri = UCrop.getOutput(it)
//                        resultUri?.let { finalUri ->
//                            handleCroppedImage(finalUri)
//                        }
//                    }
//                }
//            }
    }

    private fun bankListAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@NewSendPaymentActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.bankList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>, response: Response<JsonObject>
                ) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401 || response.code() == 402) {
                        Utils.forceLogout(
                            this@NewSendPaymentActivity,
                            response.code()
                        )  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())

                            bankList = response.body()!!.getAsJsonArray("data").map {
                                Gson().fromJson(it, BankDetail::class.java)
                            } as ArrayList<BankDetail>

                            if (bankList!!.isEmpty()) {
                                Toast.makeText(
                                    this@NewSendPaymentActivity,
                                    "No bank found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@NewSendPaymentActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }


                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@NewSendPaymentActivity,
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

    private fun invoiceListAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@NewSendPaymentActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.invoiceList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>, response: Response<JsonObject>
                ) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401 || response.code() == 402) {
                        Utils.forceLogout(
                            this@NewSendPaymentActivity,
                            response.code()
                        )  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            invoiceList = response.body()!!.getAsJsonArray("data").map {
                                Gson().fromJson(it, InvoiceDetail::class.java)
                            } as ArrayList<InvoiceDetail>

                            if (invoiceList!!.isEmpty()) {
                                Toast.makeText(
                                    this@NewSendPaymentActivity,
                                    "No invoice found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@NewSendPaymentActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@NewSendPaymentActivity,
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

    private fun handleImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.Main) {
            // Show progress dialog if needed
            ProgressDialog.start(this@NewSendPaymentActivity)
            try {
                val compressedUri = withContext(Dispatchers.IO) {
                    // This calls the method that was crashing
                    Utils.getCompressedUri(this@NewSendPaymentActivity, uri)
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
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@NewSendPaymentActivity, "Error processing image", Toast.LENGTH_SHORT
                ).show()
            } finally {
                ProgressDialog.dismiss()
            }
        }
    }

    private fun handleImageResult(imageUri: Uri) {
        // startCrop(imageUri)
        handleImage(imageUri)
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