package com.alvimatruck.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.alvimatruck.R
import com.alvimatruck.custom.SignalRManager
import com.alvimatruck.utils.Utils.DriverVanNo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.math.abs

class LocationService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var wakeLock: PowerManager.WakeLock

    private var fusedCallback: LocationCallback? = null
    private var gpsListener: LocationListener? = null

    private var serviceLooper: Looper? = null
    private var handler: Handler? = null

    private var lastLat: Double = 0.0
    private var lastLon: Double = 0.0


    private var liveCallback: ((Double, Double) -> Unit)? = null


    override fun onBind(i: Intent?) = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService() = this@LocationService
    }

    override fun onCreate() {
        super.onCreate()

        startNotification()

        // Background processing thread (ESSENTIAL for Android 14+)
        val thread = HandlerThread("GPS_THREAD")
        thread.start()
        serviceLooper = thread.looper
        handler = Handler(serviceLooper!!)

        // Keep CPU awake even if screen OFF (offline tracking)
        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, "Alvima:GPS_LOCK"
        )
        wakeLock.acquire()

        //startNotification()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        try {
            startHybridUpdates()
        } catch (e: SecurityException) {
            // If we crash here, it means permission isn't ready yet.
            Log.e("GPS", "Permission not active yet: ${e.message}")
            stopSelf() // Stop service to prevent crash
            return
        }

    }

    override fun onStartCommand(i: Intent?, f: Int, id: Int) = START_STICKY

    // Hybrid = Fused + GPS with automatic fallback
    @SuppressLint("MissingPermission")
    private fun startHybridUpdates() {

        // 1️⃣ Last Known First (Instant indoor response)
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let {
            update(it.latitude, it.longitude)
        }
        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let {
            update(it.latitude, it.longitude)
        }

        // 2️⃣ Fused Provider (Fast when internet/wifi available)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2500L).setGranularity(Granularity.GRANULARITY_FINE)
            .setWaitForAccurateLocation(false).build()

        fusedCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    update(it.latitude, it.longitude)
                }
            }
        }
        fusedClient.requestLocationUpdates(request, fusedCallback!!, Looper.getMainLooper())

        // 3️⃣ Raw GPS — the KEY for OFFLINE success
        gpsListener = LocationListener { loc ->
            update(loc.latitude, loc.longitude)
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 3000L, 0f, gpsListener!!, serviceLooper
        )
    }

    private fun update(lat: Double, lon: Double) {
        Log.d("GPS", "Lat=$lat  Lon=$lon")

        // ✅ Send only if location changed enough (avoid spam)
        if (abs(lat - lastLat) > 0.0001 ||
            abs(lon - lastLon) > 0.0001
        ) {

            // ✅ Save last values
            lastLat = lat
            lastLon = lon

            // ✅ Store globally
            AlvimaTuckApplication.latitude = lat
            AlvimaTuckApplication.longitude = lon

            // ✅ Send to server using socket
            // SocketManager.sendLocation(lat, lon)
            // WebSocketManager.sendLocation(lat, lon)

            SignalRManager.sendLocation(
                driverId = DriverVanNo,
                lat = lat,
                lon = lon
            )

            Log.d("GPS", "✅ Location Sent to Server")
        }


        Handler(Looper.getMainLooper()).post { liveCallback?.invoke(lat, lon) }
    }

    private fun startNotification() {
        val id = "gps_channel"
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(
            NotificationChannel(id, "GPS Tracking", NotificationManager.IMPORTANCE_HIGH)
        )

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val n = NotificationCompat.Builder(this, id).setSmallIcon(R.drawable.logo)
            .setContentTitle("GPS Active").setContentText("Tracking offline/online…")
            .setOngoing(true).setContentIntent(pi).build()

        if (Build.VERSION.SDK_INT >= 34) startForeground(
            1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
        else startForeground(1, n)
    }

    override fun onDestroy() {
        fusedCallback?.let { fusedClient.removeLocationUpdates(it) }
        gpsListener?.let { locationManager.removeUpdates(it) }
        if (wakeLock.isHeld) wakeLock.release()
        serviceLooper?.quit()
        super.onDestroy()
    }
}
