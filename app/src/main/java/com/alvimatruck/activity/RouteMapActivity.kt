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
            listOf(38.8147962, 8.8989606),
            listOf(38.8094904, 8.905794),
            listOf(38.8054621, 8.907631),
            listOf(38.7976729, 8.9190994),
            listOf(38.797345, 8.925509),
            listOf(38.7954014, 8.9258199),
            listOf(38.7851446, 8.922937),
            listOf(38.7804084, 8.9222764),
            listOf(38.7782996, 8.9218771),
            listOf(38.7777911, 8.9213541),
            listOf(38.7765251, 8.9208136),
            listOf(38.775935, 8.9213435),
            listOf(38.7753128, 8.9218735),
            listOf(38.7741326, 8.9218523),
            listOf(38.7726413, 8.9233256),
            listOf(38.7702918, 8.9260163),
            listOf(38.7682319, 8.9288568),
            listOf(38.7681665, 8.9304999),
            listOf(38.7667558, 8.9324332),
            listOf(38.7658331, 8.9341078),
            listOf(38.7646475, 8.9345698),
            listOf(38.7596693, 8.9313054),
            listOf(38.7586393, 8.9309238),
            listOf(38.7590041, 8.9239074),
            listOf(38.7584677, 8.9201765),
            listOf(38.758257, 8.919468),
            listOf(38.7583, 8.918618),
            listOf(38.7554867, 8.9204766),
            listOf(38.752078, 8.919297),
            listOf(38.7507391, 8.9174347),
            listOf(38.7508201, 8.9129879),
            listOf(38.7507235, 8.9124579),
            listOf(38.7508952, 8.9111648),
            listOf(38.7501441, 8.9108468),
            listOf(38.7504239, 8.9095536),
            listOf(38.7506591, 8.9087428),
            listOf(38.7504709, 8.9083078),
            listOf(38.7507779, 8.9071899),
            listOf(38.75049, 8.906323),
            listOf(38.7491794, 8.9037133),
            listOf(38.7487977, 8.9031834),
            listOf(38.7508745, 8.9005864),
            listOf(38.7514431, 8.8980636),
            listOf(38.751368, 8.8886934),
            listOf(38.752644, 8.8857997),
            listOf(38.7574727, 8.8818033),
            listOf(38.7623134, 8.8860107),
            listOf(38.7633111, 8.8864029),
            listOf(38.7639717, 8.8868495),
            listOf(38.7643644, 8.8875863),
            listOf(38.7673148, 8.8898759),
            listOf(38.7692138, 8.8909359),
            listOf(38.7706622, 8.8919217),
            listOf(38.7710126, 8.8930988),
            listOf(38.7729867, 8.8946782),
            listOf(38.7741776, 8.8932366),
            listOf(38.7750181, 8.8913387),
            listOf(38.7759408, 8.8887099),
            listOf(38.7764773, 8.8851271),
            listOf(38.7768849, 8.8835159),
            listOf(38.7790736, 8.8799542),
            listOf(38.7816271, 8.8771557),
            listOf(38.7830004, 8.8764137),
            listOf(38.7841154, 8.8782508),
            listOf(38.7917543, 8.8833813),
            listOf(38.7931705, 8.8853317),
            listOf(38.8058562, 8.8948903),
            listOf(38.808299, 8.896681),
            listOf(38.812333, 8.8985041),
            listOf(38.8147962, 8.8989606)
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