package com.alvimatruck.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityRouteMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions

class RouteMapActivity : BaseActivity<ActivityRouteMapBinding>(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
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

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
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
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        checkPermissions()

        // ✅ Your GeoJSON coordinates (longitude, latitude)
        val coordinates = listOf(
            listOf(37.9962158, 9.5357490),
            listOf(37.7984619, 9.3406722),
            listOf(37.6226807, 8.7765107),
            listOf(38.0621338, 8.1027386),
            listOf(38.5400391, 7.7109917),
            listOf(38.9575195, 7.8797060),
            listOf(39.5397949, 8.0809847),
            listOf(39.7979736, 8.8959260),
            listOf(39.3200684, 9.3027278),
            listOf(38.6663818, 9.4436432),
            listOf(37.9962158, 9.5357490)
        )

        // ✅ Convert to LatLng (reverse order)
        val latLngList = coordinates.map { LatLng(it[1], it[0]) }

        drawPolygon(latLngList)

        mMap.addMarker(
            MarkerOptions()
                .position(LatLng(8.9964938, 38.7388096))
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_truck_marker))
                .anchor(0.5f, 0.5f) // center the marker
        )
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
        vectorDrawable.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun drawPolygon(points: List<LatLng>) {
        if (points.isEmpty()) return

        val polygonOptions = PolygonOptions()
            .addAll(points)
            .strokeColor(0xFF2196F3.toInt()) // blue border
            .strokeWidth(4f)
            .fillColor(0x552196F3) // semi-transparent blue fill

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