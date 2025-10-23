package com.alvimatruck.activity

import android.os.Bundle
import android.util.Log
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityMapRouteBinding
import com.alvimatruck.utils.Utils
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
    private val origin = LatLng(
        Utils.currentLocation!!.latitude,
        Utils.currentLocation!!.longitude
    )   // Starting point
    private val destination = LatLng(23.00154667352913, 72.55099309477606)// Ending point


    override fun inflateBinding(): ActivityMapRouteBinding {
        return ActivityMapRouteBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add markers
        mMap.addMarker(MarkerOptions().position(origin).title("Start"))
        mMap.addMarker(MarkerOptions().position(destination).title("Destination"))

        // Move camera to show both points
        val bounds = LatLngBounds.builder()
            .include(origin)
            .include(destination)
            .build()
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

        // Fetch and draw the route
        getRoute(origin, destination)
    }

    private fun getRoute(origin: LatLng, dest: LatLng) {
        val apiKey = getString(R.string.googlemapkey)
        //val apiKey = "AIzaSyBGkMG-nj0pgK1ruZRZUdLW-7SgSkmqQfQ"
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false&mode=driving&key=$apiKey"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MapRoute", "Direction API Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonData ->
                    val jsonObject = JSONObject(jsonData)
                    val routes = jsonObject.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val points = decodePolyline(
                            routes.getJSONObject(0)
                                .getJSONObject("overview_polyline")
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
        val polylineOptions = PolylineOptions()
            .addAll(points)
            .width(8f)
            .color(0xFF1976D2.toInt()) // blue color
            .geodesic(true)
        mMap.addPolyline(polylineOptions)
    }

}