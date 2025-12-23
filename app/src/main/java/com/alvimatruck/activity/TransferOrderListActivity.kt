package com.alvimatruck.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.TransferListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityTransferOrderListBinding
import com.alvimatruck.model.responses.LocationDetail
import com.alvimatruck.model.responses.TransferDetail
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransferOrderListActivity : BaseActivity<ActivityTransferOrderListBinding>() {
    private var transferListAdapter: TransferListAdapter? = null

    var transferList: ArrayList<TransferDetail>? = ArrayList()

    var filterList: ArrayList<TransferDetail>? = ArrayList()
    var userDetail: UserDetail? = null

    var locationList: ArrayList<LocationDetail>? = ArrayList()


    override fun inflateBinding(): ActivityTransferOrderListBinding {
        return ActivityTransferOrderListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        userDetail =
            Gson().fromJson(SharedHelper.getKey(this, Constants.UserDetail), UserDetail::class.java)

        binding.rvTransferList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {

                filterList!!.clear()
                if (s.toString().trim().isEmpty()) {
                    filterList!!.addAll(transferList!!)
                } else {
                    for (item in transferList!!) {
                        if (item.transferOrderNo.lowercase()
                                .contains(s.toString().lowercase())
                        ) {
                            filterList!!.add(item)
                        }
                    }
                }
                transferListAdapter!!.notifyDataSetChanged()
            }
        })


        transferListAPI()
        getToLocationList()

    }

    private fun getToLocationList() {
        val jsonString = SharedHelper.getKey(this, Constants.API_To_Location)
        if (jsonString.isNotEmpty()) {
            locationList =
                JsonParser.parseString(jsonString).asJsonObject.getAsJsonArray("data").map {
                    Gson().fromJson(it, LocationDetail::class.java)
                } as ArrayList<LocationDetail>
        }

    }

    private fun transferListAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@TransferOrderListActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.transferList(userDetail?.salesPersonCode!!)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        ProgressDialog.dismiss()
                        if (response.code() == 401) {
                            Utils.forceLogout(this@TransferOrderListActivity)  // show dialog before logout
                            return
                        }
                        if (response.isSuccessful) {
                            try {
                                Log.d("TAG", "onResponse: " + response.body().toString())

                                transferList = response.body()!!.getAsJsonArray("data").map {
                                    Gson().fromJson(it, TransferDetail::class.java)
                                } as ArrayList<TransferDetail>
                                filterList = ArrayList(transferList!!)
                                if (filterList!!.isNotEmpty()) {
                                    binding.rvTransferList.layoutManager =
                                        LinearLayoutManager(
                                            this@TransferOrderListActivity,
                                            LinearLayoutManager.VERTICAL,
                                            false
                                        )


                                    transferListAdapter = TransferListAdapter(
                                        this@TransferOrderListActivity,
                                        filterList!!, locationList
                                    ) { allSelected ->
                                        // This code runs when a single item in the list is clicked.
                                        // We check if the UI needs updating to avoid infinite loops.
                                        if (binding.chkAll.isChecked != allSelected) {
                                            binding.chkAll.isChecked = allSelected
                                        }
                                    }
                                    binding.rvTransferList.adapter = transferListAdapter

                                    binding.chkAll.setOnClickListener {
                                        val isChecked = binding.chkAll.isChecked
                                        transferListAdapter?.selectAll(isChecked)
                                    }
                                    binding.llData.visibility = View.VISIBLE
                                    binding.llNoData.root.visibility = View.GONE

                                } else {
                                    binding.llData.visibility = View.GONE
                                    binding.llNoData.root.visibility = View.VISIBLE
                                }


                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                this@TransferOrderListActivity,
                                Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@TransferOrderListActivity,
                            getString(com.alvimatruck.R.string.api_fail_message),
                            Toast.LENGTH_SHORT
                        ).show()
                        ProgressDialog.dismiss()
                    }
                })
        } else {
            Toast.makeText(
                this,
                getString(com.alvimatruck.R.string.please_check_your_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}