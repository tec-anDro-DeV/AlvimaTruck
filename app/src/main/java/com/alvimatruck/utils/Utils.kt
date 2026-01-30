package com.alvimatruck.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.alvimatruck.R
import com.alvimatruck.activity.LoginActivity
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Date


@SuppressLint("SimpleDateFormat")
object Utils {
    var isTripInProgress: Boolean = false
    var isNewOrder: Boolean = false

    private val ETHIOPIA_MOBILE_REGEX = Regex("^0[79]\\d{8}$")
    private val ETHIOPIA_ANY_LOCAL_REGEX = Regex("^0\\d{9}$")


    val MAX_FILE_SIZE_BYTES = 1 * 1024 * 1024

    const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    const val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES
    const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE

    fun isValidEthiopiaMobile(number: String): Boolean {
        // remove spaces if user types "0912 345 678"
        val cleaned = number.replace("\\s".toRegex(), "")
        return ETHIOPIA_MOBILE_REGEX.matches(cleaned)
    }

    fun isValidEthiopiaLocalNumber(number: String): Boolean {
        val cleaned = number.replace("\\s".toRegex(), "")
        return ETHIOPIA_ANY_LOCAL_REGEX.matches(cleaned)
    }

    fun createFilePart(fieldName: String, uri: Uri?, context: Context): MultipartBody.Part? {
        if (uri == null) return null

        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileBytes = inputStream.readBytes()
        inputStream.close()

        val requestBody = fileBytes.toRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(
            fieldName, "file_${System.currentTimeMillis()}.jpg", requestBody
        )
    }

    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
        vectorDrawable.setBounds(
            0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight
        )

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    fun getDummyArrayList(counter: Int): ArrayList<String> {
        val stringArrayList = ArrayList<String>()
        for (i in 0 until counter) {
            stringArrayList.add("")
        }
        return stringArrayList
    }

    fun getDummyTransferList(counter: Int): ArrayList<TransferItem> {
        val rawList = getDummyArrayList(counter)
        val transferList = ArrayList<TransferItem>()
        transferList.clear()
        for (i in rawList) {
            transferList.add(TransferItem(i))
        }
        return transferList
    }

    fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string() ?: return "Something went wrong"
            val errorJson = JSONObject(errorBody)

            // 🔹NEW → First check for validation error block
            if (errorJson.has("errors")) {
                val errorsObj = errorJson.getJSONObject("errors")
                val keys = errorsObj.keys()
                val list = mutableListOf<String>()

                while (keys.hasNext()) {
                    val key = keys.next()
                    val arr = errorsObj.getJSONArray(key)
                    list.add(arr.getString(0)) // take first error only
                }

                return list.joinToString("\n") // multiple lines
            }

            // 🔹OLD Requirement → message key support
            errorJson.optString(
                "message", errorJson.optString("title", "Something went wrong") // fallback title
            )

        } catch (_: Exception) {
            "Something went wrong"
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getFullDate(time: Long?): String {
        return SimpleDateFormat("dd MMM, yyyy").format(Date(time!!))
    }


    @SuppressLint("SimpleDateFormat")
    fun getFullDateWithTime(time: Long?): String {
        return SimpleDateFormat("MM/dd/yyyy HH:mm").format(Date(time!!))
    }


    fun hideSoftKeyboard(activity: Activity, view: View) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }


    fun forceLogout(context: Context) {
        if (context !is Activity) {
            logout(context) // fallback directly
            return
        }


        val inflater = context.layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_logout2, null)

        val btnYes = alertLayout.findViewById<TextView>(R.id.btnYes)


        val dialog = AlertDialog.Builder(context).setView(alertLayout).setCancelable(false).create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        btnYes.setOnClickListener {
            dialog.dismiss()
            logout(context) // <-- perform logout
        }

        dialog.show()

        val width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }


    fun logout(context: Context) {
        SharedHelper.putKey(
            context, Constants.IS_LOGIN, false
        )

        val points = listOf(
            Constants.Token,
            Constants.UserDetail,
            Constants.API_Price_Group,
            Constants.API_City,
            Constants.API_Location_Code,
            Constants.API_Payment_Code,
            Constants.API_Item_List,
            Constants.API_Route_Cancel_Reason_List,
            Constants.API_Visit_Reason_List,
            Constants.API_CostCenter_Code,
            Constants.API_ProfitCenter_Code,
            Constants.API_Intransit_Code
        )
        points.forEach { point ->
            SharedHelper.putKey(
                context, point, ""
            )
        }


        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
        // activity.finishAffinity()
    }

    fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1 && cursor.moveToFirst()) {
                return cursor.getLong(sizeIndex)
            }
        }
        return -1L // Return -1 if size cannot be determined
    }

    fun distanceInKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): String {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return (result[0] / 1000).toDouble().to2Decimal()
    }

    fun getCompressedUri(context: Context, uri: Uri): Uri {
        val fileSize = getFileSizeFromUri(context, uri)
        // Return original URI if size is unknown or already within the limit
        if (fileSize == -1L || fileSize <= MAX_FILE_SIZE_BYTES) {
            return uri
        }

        // Prepare the output file for the compressed image in the app's cache directory
        val compressedFileName = "compressed_${System.currentTimeMillis()}.jpg"
        val compressedFile = File(context.cacheDir, compressedFileName)

        // Use a try-with-resources block to auto-close streams
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(compressedFile).use { outputStream ->
                    // Decode and compress the bitmap
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG, 80, outputStream
                    ) // Adjust quality (0-100)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // In case of an error, return the original URI
            return uri
        }

        // Return the content URI for the newly created compressed file
        return FileProvider.getUriForFile(
            context, "${context.packageName}.provider", compressedFile
        )
    }

    fun loadProfileWithPlaceholder(
        context: Context, imageView: ImageView, name: String, imageUrl: String?
    ) {
        val firstChar = name.firstOrNull()?.let { (it.uppercaseChar()).toString() }

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context).load(Constants.IMAGE_URL + imageUrl).circleCrop().error(
                BitmapDrawable(
                    context.resources, generateCircleLetterBitmap(context, firstChar.toString())
                )
            ).into(imageView)
        } else {
            imageView.setImageBitmap(generateCircleLetterBitmap(context, firstChar.toString()))
        }
    }

    fun generateCircleLetterBitmap(context: Context, letter: String, size: Int = 120): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paintCircle = Paint().apply {
            color = ContextCompat.getColor(context, R.color.orange)
            isAntiAlias = true
        }

        val paintText = Paint().apply {
            color = Color.WHITE
            textSize = size / 2.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }

        // Draw circle
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paintCircle)

        // Draw letter in center
        val textY = size / 2f - (paintText.descent() + paintText.ascent()) / 2
        canvas.drawText(letter, size / 2f, textY, paintText)

        return bitmap
    }

    fun Double.to2Decimal(): String {
        return BigDecimal(this).setScale(2, RoundingMode.HALF_UP).toDouble().toString()
    }

    fun getFormatedRequestDate(dateStr: String): String {
        val finalDate = if (dateStr.length == 10) {
            dateStr + "T00:00:00"
        } else dateStr
        return LocalDateTime.parse(
            finalDate,
            DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T'HH:mm:ss").optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true).optionalEnd().toFormatter()
        ).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}

data class TransferItem(
    var name: String, var isSelected: Boolean = false
)