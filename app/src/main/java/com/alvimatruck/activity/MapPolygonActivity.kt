package com.alvimatruck.activity

import android.os.Bundle
import com.alvimatruck.R
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityMapPolygonBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolygonOptions

class MapPolygonActivity : BaseActivity<ActivityMapPolygonBinding>(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    override fun inflateBinding(): ActivityMapPolygonBinding {
        return ActivityMapPolygonBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // ✅ Your GeoJSON coordinates (longitude, latitude)
        val coordinates = listOf(
            listOf(72.5430465, 23.0241714),
            listOf(72.5431967, 23.0245071),
            listOf(72.5433683, 23.0249613),
            listOf(72.5436258, 23.025139),
            listOf(72.5437331, 23.0253958),
            listOf(72.5439906, 23.0257907),
            listOf(72.5443554, 23.0259882),
            listOf(72.5446558, 23.0265016),
            listOf(72.5449991, 23.0266794),
            listOf(72.5455999, 23.0265609),
            listOf(72.5458574, 23.0262252),
            listOf(72.5462008, 23.0259684),
            listOf(72.5465012, 23.0256722),
            listOf(72.546823, 23.0255735),
            listOf(72.5471878, 23.025218),
            listOf(72.5473809, 23.0249613),
            listOf(72.5479174, 23.0245861),
            listOf(72.5483465, 23.0241911),
            listOf(72.5489259, 23.0239541),
            listOf(72.5491834, 23.0235197),
            listOf(72.5497842, 23.0231247),
            listOf(72.5497413, 23.0226902),
            listOf(72.5501704, 23.0224137),
            listOf(72.5493336, 23.022157),
            listOf(72.5484109, 23.022078),
            listOf(72.5472736, 23.0217818),
            listOf(72.546351, 23.0217028),
            listOf(72.5455356, 23.021683),
            listOf(72.5451279, 23.021762),
            listOf(72.5442696, 23.0219398),
            listOf(72.5435615, 23.0219595),
            listOf(72.5431967, 23.022078),
            listOf(72.543025, 23.0221768),
            listOf(72.5428534, 23.0223742),
            listOf(72.5428534, 23.0229865),
            listOf(72.5429821, 23.0232629),
            listOf(72.5429821, 23.0235592),
            listOf(72.5430036, 23.0238159),
            listOf(72.5430679, 23.0240529)
        )

        // ✅ Convert to LatLng (reverse order)
        val latLngList = coordinates.map { LatLng(it[1], it[0]) }

        drawPolygon(latLngList)
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