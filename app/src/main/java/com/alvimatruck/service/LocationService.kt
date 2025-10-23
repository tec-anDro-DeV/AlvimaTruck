package com.alvimatruck.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.alvimatruck.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService : Service() {

    private val TAG = LocationService::class.java.name

    private lateinit var fusedClient: FusedLocationProviderClient
    private var locationCallback: ((Location) -> Unit)? = null

    fun setLocationCallback(callback: (Location) -> Unit) {
        locationCallback = callback
    }

    private val binder = LocalBinder()


    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }


    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")

        // preferences = Preferences.getInstance(this@LocationService)
        //  db = LexonDatabase(this@LocationService)
        startForegroundServiceNotification()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }


    private fun startForegroundServiceNotification() {
        val channelId = "location_channel"
        val channelName = "Location Tracking"
        val notificationManager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId, channelName, NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Service Active")
            .setContentText("Tracking your location in background")
            .setSmallIcon(R.drawable.app_logo)
            .setOngoing(true)
            .build()

        // ✅ Required for Android 14+ / targetSdk ≥ 34
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(1, notification)
        }
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).build()

        val fusedLocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                Log.d("LocationService", "Lat=${location.latitude}, Lon=${location.longitude}")

                // ✅ Invoke callback for activity
                locationCallback?.invoke(location)
            }
        }

        fusedClient.requestLocationUpdates(request, fusedLocationCallback, Looper.getMainLooper())
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(object : LocationCallback() {})
    }

//
//    private fun updateUI() {
//        val currentDate = Date()
//        latitude = mLastLocation!!.latitude
//        longitude = mLastLocation!!.longitude
//        if (checkNetworkConnection(applicationContext)) {
//            try {
//                val locationData = JSONObject()
//                locationData.put("lat", latitude.toString())
//                locationData.put("longs", longitude.toString())
//                locationData.put("loginDate", SimpleDateFormat("dd/MM/yyyy").format(currentDate))
//                locationData.put("loginTime", SimpleDateFormat("HH:mm:ss").format(currentDate))
//                postLocationData(locationData)
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                db!!.insertTrackedLocation(
//                    preferences!!.getString(ConstantKeys.KEY_USER_ID),
//                    latitude.toString(),
//                    longitude.toString(),
//                    SimpleDateFormat("dd/MM/yyyy").format(currentDate),
//                    SimpleDateFormat("HH:mm:ss").format(currentDate)
//                )
//                db!!.closeDatabase()
//            }
//        } else {
//            db!!.insertTrackedLocation(
//                preferences!!.getString(ConstantKeys.KEY_USER_ID),
//                latitude.toString(),
//                longitude.toString(),
//                SimpleDateFormat("dd/MM/yyyy").format(currentDate),
//                SimpleDateFormat("HH:mm:ss").format(currentDate)
//            )
//            db!!.closeDatabase()
//        }
//    }
//
//    private fun postLocationData(locationData: JSONObject) {
//        try {
//            val imeistring: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                "0"
//            } else {
//                try {
//                    val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
//                    if (ActivityCompat.checkSelfPermission(
//                            this,
//                            Manifest.permission.READ_PHONE_STATE
//                        ) != PackageManager.PERMISSION_GRANTED
//                    ) {
//                        return
//                    }
//                    telephonyManager.deviceId
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    "0"
//                }
//            }
//            val locObj = JSONObject()
//            locObj.put("driverCode", preferences!!.getString(ConstantKeys.KEY_USER_ID))
//            locObj.put("deviceCode", imeistring)
//            val jsonArray = JSONArray()
//            jsonArray.put(locationData)
//            locObj.put("latlong", jsonArray)
//            Log.i(TAG, locObj.toString())
//            if (jsonArray.length() > 0) {
//                val webServiceHandlers = WebServiceHandler.getInstance(this, false)
//                webServiceHandlers.init(
//                    "postLatlongs",
//                    WebServiceHandler.RequestType.POST,
//                    locObj,
//                    latLongPostCallBackListener
//                )
//            }
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//    }
}