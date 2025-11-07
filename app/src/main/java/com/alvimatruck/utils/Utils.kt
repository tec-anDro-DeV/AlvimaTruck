package com.alvimatruck.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import org.json.JSONObject
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@SuppressLint("SimpleDateFormat")
object Utils {
    var mLastClickTime: Long = 0
    var token: String = ""
    var currentLocation: Location? = null


    fun getDummyArrayList(counter: Int): ArrayList<String> {
        val stringArrayList =
            ArrayList<String>()
        for (i in 0 until counter) {
            stringArrayList.add("")
        }
        return stringArrayList
    }

    fun parseErrorMessage(response: Response<*>): String {
        return try {
            JSONObject(response.errorBody()?.string() ?: "").optString(
                "message",
                "Something went wrong"
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
    fun getMinutes(time: Long?): String {
        val formatter = SimpleDateFormat("mm")
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(time!!))
    }

    @SuppressLint("SimpleDateFormat")
    fun getSeconds(time: Long?): String {
        return SimpleDateFormat("ss", Locale.ENGLISH).format(Date(time!!))
    }

    @SuppressLint("SimpleDateFormat")
    fun getFullDateYearFirst(time: Long?): String {
        return SimpleDateFormat("yyyy-MM-dd").format(Date(time!!))
    }

    @SuppressLint("SimpleDateFormat")
    fun getFullTime(time: Long?): String {
        val df1 = SimpleDateFormat("hh:mm a")
        df1.timeZone = TimeZone.getDefault()
        return df1.format(Date(time!! * 1000))
    }

    @SuppressLint("SimpleDateFormat")
    fun getFullDateWithTime(time: Long?): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(time!!))
    }


    fun dateFormatChange(dateStr: String, input: String, output: String): String {
        return SimpleDateFormat(output, Locale.ENGLISH).format(
            SimpleDateFormat(
                input,
                Locale.ENGLISH
            ).parse(dateStr)
        )
    }

    fun getMilliFromDate(dateFormat: String?): Long {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date: Date = formatter.parse(dateFormat)
        return date.time
    }

    fun getMilliFromHoldDate(dateFormat: String?): Long {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date: Date = formatter.parse(dateFormat)
        return date.time
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


    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }


    fun getMonth3LettersName(date: Date): String =
        SimpleDateFormat("MMM", Locale.getDefault()).format(date)

    fun getDayNumber(date: Date): String =
        SimpleDateFormat("dd", Locale.getDefault()).format(date)


    fun getDaysDifferent(time: Long): Int {
        return ((System.currentTimeMillis() - time) / (1000 * 60 * 60 * 24)).toInt()
    }


//    fun logoutDialog(context: Activity, message: String) {
//        val builder = android.app.AlertDialog.Builder(context)
//        builder.setTitle(R.string.logout)
//        builder.setMessage(message)
//
//        //performing positive action
//        builder.setPositiveButton(context.getString(R.string.yes)) { dialogInterface, which ->
//        
//            SharedHelper.putKey(
//                context,
//                Constants.IS_LOGIN,
//                false
//            )
//            SharedHelper.clearSharedPreferences(context)
//            val notificationManager: NotificationManager =
//                context.applicationContext.getSystemService(
//                    Context.NOTIFICATION_SERVICE
//                ) as NotificationManager
//            notificationManager.cancelAll()
//            val intent = Intent(context, LandingActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            context.startActivity(intent)
//            context.finishAffinity()
//
//            builder.setCancelable(true)
//        }
//        builder.setCancelable(false)
//        builder.show()
//    }


    fun getTimeAgo(mTime: Long): String {
        val SECOND_MILLIS = 1000
        val MINUTE_MILLIS = 60 * SECOND_MILLIS
        val HOUR_MILLIS = 60 * MINUTE_MILLIS
        val DAY_MILLIS = 24 * HOUR_MILLIS
        var time = mTime
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }
        val now: Long = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return "just now"
        }

        // TODO: localize
        val diff = now - time
        return if (diff < MINUTE_MILLIS) {
            "just now"
        } else if (diff < 2 * MINUTE_MILLIS) {
            "a minute ago"
        } else if (diff < 50 * MINUTE_MILLIS) {
            (diff / MINUTE_MILLIS).toString() + " minutes ago"
        } else if (diff < 90 * MINUTE_MILLIS) {
            "an hour ago"
        } else if (diff < 24 * HOUR_MILLIS) {
            (diff / HOUR_MILLIS).toString() + " hours ago"
        } else {
            (diff / DAY_MILLIS).toString() + " days ago"
        }
    }
}