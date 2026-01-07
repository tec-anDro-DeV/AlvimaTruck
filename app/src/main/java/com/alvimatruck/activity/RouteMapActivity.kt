package com.alvimatruck.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityRouteMapBinding
import com.alvimatruck.model.responses.RouteDetail
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
import com.google.android.gms.maps.model.PolygonOptions
import com.google.gson.Gson

class RouteMapActivity : BaseActivity<ActivityRouteMapBinding>(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    var routeDetail: RouteDetail? = null

    private var latLngList: List<LatLng> = emptyList()


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // Permission was granted. Enable the user's location.
            enableMyLocation()
        } else {
            // Optionally, handle the case where the user denies the permission.
        }
    }


    override fun inflateBinding(): ActivityRouteMapBinding {
        return ActivityRouteMapBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        checkAndStartLocationService()

        if (intent != null) {
            routeDetail = Gson().fromJson(
                intent.getStringExtra(Constants.RouteDetail).toString(), RouteDetail::class.java
            )
            binding.tvTitle.text = "Route " + routeDetail!!.routeName
            latLngList = routeDetail!!.locations.map {
                LatLng(it.latitude, it.longitude)
            }
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    private fun checkPermissions() {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            // Permissions are already granted, enable location.
            enableMyLocation()
        } else {
            // Permissions are not granted, request them.
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        // This function is only called after the permission check has passed.
        // The @SuppressLint annotation is used to suppress the Lint warning.
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        mMap.setLocationSource(object : com.google.android.gms.maps.LocationSource {
            override fun activate(listener: com.google.android.gms.maps.LocationSource.OnLocationChangedListener) {
                // Do nothing. We don't pass location updates to the map layer.
                // This prevents the blue dot from being drawn/updated.
            }

            override fun deactivate() {
                // Cleanup if needed
            }
        })

        mMap.setOnMyLocationButtonClickListener {
            // Get the actual location from your app's tracking or the LocationManager
            val currentLat = AlvimaTuckApplication.latitude
            val currentLng = AlvimaTuckApplication.longitude

            if (currentLat != 0.0 && currentLng != 0.0) {
                val latLng = LatLng(currentLat, currentLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
            }
            // Return true to consume the event so the map doesn't try to default handle it
            true
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        checkPermissions()


        drawPolygon(latLngList)

        mMap.addMarker(
            MarkerOptions().position(
                LatLng(
                    AlvimaTuckApplication.latitude, AlvimaTuckApplication.longitude
                )
            ).icon(bitmapDescriptorFromVector(this, R.drawable.ic_truck_marker))
                .anchor(0.5f, 0.5f) // center the marker
        )
    }


    private fun drawPolygon(points: List<LatLng>) {
        if (points.isEmpty()) return

        val polygonOptions =
            PolygonOptions().addAll(points).strokeColor(0xFF2196F3.toInt()) // blue border
                .strokeWidth(4f).fillColor(0x552196F3) // semi-transparent blue fill

        mMap.addPolygon(polygonOptions)

        // Move camera to polygon bounds
        val boundsBuilder = LatLngBounds.builder()
        for (latLng in points) {
            boundsBuilder.include(latLng)
        }
        val bounds = boundsBuilder.build()
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }
}