package com.alvimatruck.activity

import android.os.Bundle
import android.util.Log
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityMapRouteBinding
import com.alvimatruck.service.AlvimaTuckApplication
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.Utils.bitmapDescriptorFromVector
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MapRouteActivity : BaseActivity<ActivityMapRouteBinding>(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val client = OkHttpClient()

    // Example points (Ahmedabad)
    var origin: LatLng? = null // Starting point
    private var destination = LatLng(23.071593, 72.5869836)// Ending point
    var customerName: String? = null

    private val updateHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateLocationAndRoute()
            updateHandler.postDelayed(this, 10000) // Repeat every 10 seconds
        }
    }

    // Track the current marker so we can remove/move it
    private var originMarker: com.google.android.gms.maps.model.Marker? = null
    private var currentPolyline: com.google.android.gms.maps.model.Polyline? = null


    override fun inflateBinding(): ActivityMapRouteBinding {
        return ActivityMapRouteBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (intent != null) {
            destination = LatLng(
                intent.getDoubleExtra(Constants.LATITUDE, 0.0),
                intent.getDoubleExtra(Constants.LONGITUDE, 0.0)
            )
            customerName = intent.getStringExtra(Constants.CustomerDetail)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add static destination marker once
        mMap.addMarker(MarkerOptions().position(destination).title(customerName))

        // Start the update loop immediately
        updateHandler.post(updateRunnable)
    }

    private fun updateLocationAndRoute() {
        // 1. Get latest location
        val lat = AlvimaTuckApplication.latitude
        val lng = AlvimaTuckApplication.longitude

        // If location is still 0,0, skip this update
        if (lat == 0.0 || lng == 0.0) return

        val newOrigin = LatLng(lat, lng)
        origin = newOrigin

        // 2. Update the "Start" Marker
        if (originMarker == null) {
            // Create marker if it doesn't exist
            originMarker = mMap.addMarker(
                MarkerOptions().position(newOrigin).title("Start")
                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_truck_marker))
                    .anchor(0.5f, 0.5f)
            )
        } else {
            // Move existing marker to new location
            originMarker?.position = newOrigin
        }

        // 3. Update Camera (Every time, but with a limit)
        try {
            val bounds = LatLngBounds.builder().include(newOrigin).include(destination).build()

            // Calculate the camera update for these bounds with padding
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 150)

            // Move the camera
            mMap.animateCamera(cameraUpdate, object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    // Once animation finishes, check if we are zoomed in too far
                    if (mMap.cameraPosition.zoom > 18f) {
                        // If zoom is 16, 17, 18+, force it back to 15
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(18f))
                    }
                }

                override fun onCancel() {}
            })

        } catch (e: Exception) {
            Log.e("MapRoute", "Camera update failed: ${e.message}")
        }

        // 4. Recalculate Route
        getRoute(newOrigin, destination)
    }

    private fun getRoute(origin: LatLng, dest: LatLng) {
        val apiKey = getString(R.string.googlemapkey)
        //val apiKey = "AIzaSyBGkMG-nj0pgK1ruZRZUdLW-7SgSkmqQfQ"
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?" + "origin=${origin.latitude},${origin.longitude}" + "&destination=${dest.latitude},${dest.longitude}" + "&sensor=false&mode=driving&key=$apiKey"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MapRoute", "Direction API Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body.string().let { jsonData ->
                    val jsonObject = JSONObject(jsonData)
                    val routes = jsonObject.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val points = decodePolyline(
                            routes.getJSONObject(0).getJSONObject("overview_polyline")
                                .getString("points")
                        )
                        runOnUiThread {
                            drawRoute(points)
                        }
                    }
                }
            }
        })
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }

    private fun drawRoute(points: List<LatLng>) {
        currentPolyline?.remove()
        val polylineOptions =
            PolylineOptions().addAll(points).width(8f).color(0xFF1976D2.toInt()) // blue color
                .geodesic(true)
        mMap.addPolyline(polylineOptions)
    }

    override fun onDestroy() {
        super.onDestroy()    // Stop the 10-second timer when leaving the screen
        updateHandler.removeCallbacks(updateRunnable)
    }


}