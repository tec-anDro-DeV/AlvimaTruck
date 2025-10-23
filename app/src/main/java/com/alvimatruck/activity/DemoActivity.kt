package com.alvimatruck.activity

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.adapter.DemoSingleItemSelectionAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityDemoBinding
import com.alvimatruck.service.LocationService


class DemoActivity : BaseActivity<ActivityDemoBinding>() {


    override fun inflateBinding(): ActivityDemoBinding {
        return ActivityDemoBinding.inflate(layoutInflater)
    }

    private var locationService: LocationService? = null
    private var isBound = false  // ✅ Add this flag
    var itemList: ArrayList<String>? = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as LocationService.LocalBinder
            locationService = localBinder.getService()
            isBound = true

            locationService!!.setLocationCallback { location ->
                runOnUiThread {
                    binding.tvCurrentLocation.text =
                        "Lat: ${location.latitude}\nLon: ${location.longitude}"
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            locationService = null
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            try {
                unbindService(connection)
                isBound = false
            } catch (_: Exception) {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        itemList!!.add("Item 1")
        itemList!!.add("Item 2")
        itemList!!.add("Item 3")
        itemList!!.add("Item 4")
        itemList!!.add("Item 5")
        itemList!!.add("Item 6")


        checkAllPermissionsAndStartService()

        binding.btnOpenMap.setOnClickListener {
            startActivity(Intent(this, MapPolygonActivity::class.java))
        }
        binding.btnOpenRoutePath.setOnClickListener {
            startActivity(Intent(this, MapRouteActivity::class.java))
        }

        binding.btnOpenList.setOnClickListener {
            startActivity(Intent(this, DemoListActivity::class.java))
        }

        binding.btnOpenImagePicker.setOnClickListener {
            startActivity(Intent(this, ImagePickerActivity::class.java))
        }

        binding.btnOpenOnBoarding.setOnClickListener {
            startActivity(Intent(this, OnBoardingActivity::class.java))
        }

        binding.btnOpenSearchDialogList.setOnClickListener {
            filterList!!.clear()
            dialog_single_selection(itemList!!)
        }
    }

    private fun checkAllPermissionsAndStartService() {
        when {
            !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || !hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 100
                )
            }

            !hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 101
                )
            }

            Build.VERSION.SDK_INT >= 34 && !hasPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION) -> {
                requestPermissions(
                    arrayOf(Manifest.permission.FOREGROUND_SERVICE_LOCATION), 102
                )
            }

            else -> {
                startAndBindService()
            }
        }
    }

    private fun startAndBindService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            // continue checking next level
            checkAllPermissionsAndStartService()
        } else {
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("Please grant location permissions to use this feature.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }.setNegativeButton("Cancel", null).show()
    }


    private fun dialog_single_selection(list: ArrayList<String>) {
        filterList!!.addAll(list)
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_single_selection, null)

        var productSeletionsAdapter = DemoSingleItemSelectionAdapter(this, filterList!!, "")

        val lLayout = LinearLayoutManager(this)
        val rvBinList = alertLayout.findViewById<RecyclerView>(R.id.rvBinList)
        rvBinList.layoutManager = lLayout
        rvBinList.adapter = productSeletionsAdapter
        val etBinSearch = alertLayout.findViewById<EditText>(R.id.etBinSearch)



        etBinSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                //filter(s.toString())
                filterList!!.clear()
                if (s.toString().trim().isEmpty()) {
                    filterList!!.addAll(list)
                } else {
                    for (item in list) {
                        if (item.lowercase().contains(s.toString().lowercase())) {
                            filterList!!.add(item)
                        }
                    }
                }
                productSeletionsAdapter = DemoSingleItemSelectionAdapter(
                    this@DemoActivity, filterList!!, ""
                )
                rvBinList.adapter = productSeletionsAdapter
            }
        })

        val tv_cancel = alertLayout.findViewById<TextView>(R.id.tvCancel2)
        val tv_confirm = alertLayout.findViewById<TextView>(R.id.tvConfirm2)

        val alert = AlertDialog.Builder(this)
        alert.setView(alertLayout)
        alert.setCancelable(false)

        val dialog = alert.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        tv_cancel.setOnClickListener { view: View? -> dialog.dismiss() }
        tv_confirm.setOnClickListener { view: View? ->
            // binding.tvChangeBin.text = productSeletionsAdapter.selected
            dialog.dismiss()
        }
    }

}

