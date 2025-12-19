package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.alvimatruck.R
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityHomeBinding
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    var userDetail: UserDetail? = null
    override fun inflateBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvDate.text = Utils.getFullDate(System.currentTimeMillis())

        userDetail =
            Gson().fromJson(SharedHelper.getKey(this, Constants.UserDetail), UserDetail::class.java)
        binding.tvUsername.text = userDetail?.firstName + " " + userDetail?.lastName
        binding.rlBottomHome.setOnClickListener {
//            binding.rlBottomHome.setBackgroundResource(R.drawable.orange_circle)
//            binding.rlBottomTrip.setBackgroundResource(0)
//            binding.rlBottomVanStock.setBackgroundResource(0)
//            binding.rlBottomOpreation.setBackgroundResource(0)
        }

        binding.rlBottomTrip.setOnClickListener {
//            binding.rlBottomHome.setBackgroundResource(0)
//            binding.rlBottomTrip.setBackgroundResource(R.drawable.orange_circle)
//            binding.rlBottomVanStock.setBackgroundResource(0)
//            binding.rlBottomOpreation.setBackgroundResource(0)
        }


        binding.rlBottomVanStock.setOnClickListener {
            startActivity(Intent(this, VanStockActivity::class.java))
//            binding.rlBottomHome.setBackgroundResource(0)
//            binding.rlBottomTrip.setBackgroundResource(0)
//            binding.rlBottomVanStock.setBackgroundResource(R.drawable.orange_circle)
//            binding.rlBottomOpreation.setBackgroundResource(0)
        }


        binding.rlBottomOpreation.setOnClickListener {
//            binding.rlBottomHome.setBackgroundResource(0)
//            binding.rlBottomTrip.setBackgroundResource(0)
//            binding.rlBottomVanStock.setBackgroundResource(0)
//            binding.rlBottomOpreation.setBackgroundResource(R.drawable.orange_circle)
            startActivity(Intent(this, OperationsActivity::class.java))
        }

        binding.llSalesRoute.setOnClickListener {
            startActivity(Intent(this, SalesRouteActivity::class.java))
        }

        binding.llCustomer.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    CustomersActivity::class.java
                ).putExtra(Constants.RouteDetail, "").putExtra(Constants.TripStart, false)
            )
        }

        binding.rlLogout.setOnClickListener {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_logout, null)

            val btnNo = alertLayout.findViewById<TextView>(R.id.btnNo)
            val btnYes = alertLayout.findViewById<TextView>(R.id.btnYes)

            val dialog =
                AlertDialog.Builder(this).setView(alertLayout).setCancelable(false).create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


            btnNo.setOnClickListener {
                dialog.dismiss()
            }
            btnYes.setOnClickListener {
                dialog.dismiss()
                Utils.logout(this)

            }
            dialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        binding.llProgressRoute.setOnClickListener {
//            startActivity(
//                Intent(this, RouteDetailActivity::class.java).putExtra(
//                    Constants.Status, binding.tvStatus.text.toString()
//                )
//            )
        }
        binding.llFeetManagement.setOnClickListener {
            startActivity(Intent(this, FleetManagementActivity::class.java))
        }
        binding.llReport.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        binding.rlSync.setOnClickListener {
            fetchAndCacheAllDropdowns(isManualSync = true)
        }

        fetchAndCacheAllDropdowns(isManualSync = false)

    }

    private fun isDropdownDataCached(): Boolean {
        // Check if a key for one of the dropdowns exists.
        // If it exists, we assume all are cached.
        return SharedHelper.getKey(this, Constants.API_Price_Group).isNotEmpty()
    }

    private fun fetchAndCacheAllDropdowns(isManualSync: Boolean) {
        // If it's not a manual sync and data is already cached, do nothing.
        if (!isManualSync && isDropdownDataCached()) {
            return
        }
        if (Utils.isOnline(this)) {

            // Show a progress bar to the user
            ProgressDialog.start(this)

            // Use lifecycleScope to launch a coroutine tied to this activity's lifecycle.
            lifecycleScope.launch(Dispatchers.IO) { // Use IO dispatcher for network calls
                try {
                    // List of all dropdown endpoints from your Constants file
                    val dropdownEndpoints = listOf(
                        Constants.API_Price_Group,
                        Constants.API_City,
                        Constants.API_Location_Code,
                        Constants.API_Payment_Code,
                        Constants.API_Item_List,
                        Constants.API_Route_Cancel_Reason_List,
                        Constants.API_Visit_Reason_List,
                        Constants.API_CostCenter_Code,
                        Constants.API_ProfitCenter_Code,
                        Constants.API_Intransit_Code
                    )

                    // This assumes your ApiClient and ApiService are set up correctly
                    val apiService = ApiClient.getRestClient(Constants.BASE_URL, "")?.webservices
                    dropdownEndpoints.forEach { endpoint ->
                        // Assuming a generic suspend function in your ApiService interface
                        // like: suspend fun getDropdownData(@Url url: String): Response<JsonElement>
                        val response = apiService!!.getDropdownData(endpoint)
                        if (response.isSuccessful && response.body() != null) {
                            val jsonString = Gson().toJson(response.body())
                            SharedHelper.putKey(this@HomeActivity, endpoint, jsonString)
                        }
                    }

                    // After the loop finishes, switch back to the main thread to update the UI
                    withContext(Dispatchers.Main) {
                        ProgressDialog.dismiss()
                        // Only show toast on manual sync
                        if (isManualSync) {
                            Log.d("TAG", "fetchAndCacheAllDropdowns: " + "Data successfully synced")
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        ProgressDialog.dismiss()
                        Toast.makeText(
                            this@HomeActivity,
                            "Failed to sync data",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } else {
            Toast.makeText(
                this, getString(R.string.please_check_your_internet_connection), Toast.LENGTH_SHORT
            ).show()
        }
    }

}