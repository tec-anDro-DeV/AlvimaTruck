package com.alvimatruck.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.RequisitionListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityStoreRequisitionListBinding
import com.alvimatruck.model.responses.LocationDetail
import com.alvimatruck.model.responses.RequisitionDetail
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

class StoreRequisitionListActivity : BaseActivity<ActivityStoreRequisitionListBinding>() {
    private var requisitionListAdapter: RequisitionListAdapter? = null

    var requestList: ArrayList<RequisitionDetail>? = ArrayList()

    var filterList: ArrayList<RequisitionDetail>? = ArrayList()

    var locationList: ArrayList<LocationDetail>? = ArrayList()


    override fun inflateBinding(): ActivityStoreRequisitionListBinding {
        return ActivityStoreRequisitionListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rvRequisitionList.addItemDecoration(
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
                    filterList!!.addAll(requestList!!)
                } else {
                    for (item in requestList!!) {
                        if (item.no.lowercase().contains(s.toString().lowercase())) {
                            filterList!!.add(item)
                        }
                    }
                }
                requisitionListAdapter!!.notifyDataSetChanged()
            }
        })

        binding.chkAll.setOnClickListener {
            val isChecked = binding.chkAll.isChecked
            requisitionListAdapter?.selectAll(isChecked)
        }

        getRequisitionListAPI()
        getToLocationList()

        binding.tvConfirmShipment.setOnClickListener {
            val selectedOrders = filterList?.filter { it.isSelected }
            if (selectedOrders.isNullOrEmpty()) {
                Toast.makeText(
                    this, "Please select at least one order send to approval", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else {
                val selectedOrderNumbers = selectedOrders.map { it.no }
                approvalPostAPI(selectedOrderNumbers)
            }
        }


    }

    private fun approvalPostAPI(selectedOrderNumbers: List<String>) {


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

    private fun getRequisitionListAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@StoreRequisitionListActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.getStoreRequisitionList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>, response: Response<JsonObject>
                ) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@StoreRequisitionListActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())

                            requestList = response.body()!!.getAsJsonArray("items").map {
                                Gson().fromJson(it, RequisitionDetail::class.java)
                            } as ArrayList<RequisitionDetail>
                            filterList = ArrayList(requestList!!)
                            if (filterList!!.isNotEmpty()) {
                                binding.rvRequisitionList.layoutManager = LinearLayoutManager(
                                    this@StoreRequisitionListActivity,
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )


                                requisitionListAdapter = RequisitionListAdapter(
                                    this@StoreRequisitionListActivity, filterList!!, locationList
                                ) { allSelected ->
                                    // This code runs when a single item in the list is clicked.
                                    // We check if the UI needs updating to avoid infinite loops.
                                    if (binding.chkAll.isChecked != allSelected) {
                                        binding.chkAll.isChecked = allSelected
                                    }
                                }
                                binding.rvRequisitionList.adapter = requisitionListAdapter


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
                            this@StoreRequisitionListActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@StoreRequisitionListActivity,
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