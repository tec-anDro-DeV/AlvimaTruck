package com.alvimatruck.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.TransferShipToReceiveListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityTransferShipToReceiveBinding
import com.alvimatruck.model.request.ReceiveItemRequest
import com.alvimatruck.model.responses.TransferReciveDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransferShipToReceiveActivity : BaseActivity<ActivityTransferShipToReceiveBinding>() {
    private var transferShipToReceiveListAdapter: TransferShipToReceiveListAdapter? = null

    var transferList: ArrayList<TransferReciveDetail>? = ArrayList()

    var filterList: ArrayList<TransferReciveDetail>? = ArrayList()

    override fun inflateBinding(): ActivityTransferShipToReceiveBinding {
        return ActivityTransferShipToReceiveBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.rvTransferList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )


//        binding.chkAll.setOnClickListener {
//            val isChecked = binding.chkAll.isChecked
//            transferShipToReceiveListAdapter?.selectAll(isChecked)
//        }

        transferReceiveAPI()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {

                filterList!!.clear()
                if (s.toString().trim().isEmpty()) {
                    filterList!!.addAll(transferList!!)
                } else {
                    for (item in transferList!!) {
                        if (item.documentNo.lowercase().contains(s.toString().lowercase())) {
                            filterList!!.add(item)
                        }
                    }
                }
                transferShipToReceiveListAdapter!!.notifyDataSetChanged()
            }
        })

        binding.tvConfirmReceive.setOnClickListener {
            val selectedOrders = filterList?.filter { it.isSelected }
            if (selectedOrders.isNullOrEmpty()) {
                Toast.makeText(
                    this, "Please select at least one item for confirm receive", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else {
                if (Utils.isOnline(this)) {
                    ProgressDialog.start(this@TransferShipToReceiveActivity)
                    confirmItemPostAPI(selectedOrders as ArrayList<TransferReciveDetail>, 0)
                } else {
                    Toast.makeText(
                        this,
                        getString(com.alvimatruck.R.string.please_check_your_internet_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    }

    private fun confirmItemPostAPI(
        selectedOrderNumbers: ArrayList<TransferReciveDetail>,
        index: Int
    ) {
        ApiClient.getRestClient(
            Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
        )!!.webservices.receiveItem(
            ReceiveItemRequest(
                selectedOrderNumbers[index].lineNo,
                selectedOrderNumbers[index].qtyToReceive,
                selectedOrderNumbers[index].documentNo
            )
        ).enqueue(object : Callback<JsonObject> {
            override fun onResponse(
                call: Call<JsonObject>, response: Response<JsonObject>
            ) {

                if (response.code() == 401) {
                    ProgressDialog.dismiss()
                    Utils.forceLogout(this@TransferShipToReceiveActivity)  // show dialog before logout
                    return
                }
                if (response.isSuccessful) {
                    try {
                        Log.d("TAG", "onResponse: " + response.body().toString())
                        val nextIndex = index + 1
                        if (nextIndex < selectedOrderNumbers.size) {
                            // Call next order
                            confirmItemPostAPI(selectedOrderNumbers, nextIndex)
                        } else {
                            ProgressDialog.dismiss()
                            Toast.makeText(
                                this@TransferShipToReceiveActivity,
                                response.body()!!.get("message").toString().replace('"', ' ')
                                    .trim(),
                                Toast.LENGTH_SHORT
                            ).show()
                            transferReceiveAPI()
                        }


                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    ProgressDialog.dismiss()
                    Toast.makeText(
                        this@TransferShipToReceiveActivity,
                        Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                Toast.makeText(
                    this@TransferShipToReceiveActivity,
                    getString(com.alvimatruck.R.string.api_fail_message),
                    Toast.LENGTH_SHORT
                ).show()
                ProgressDialog.dismiss()
            }
        })

    }

    private fun transferReceiveAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@TransferShipToReceiveActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.transferLines()
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>, response: Response<JsonObject>
                    ) {
                        ProgressDialog.dismiss()
                        if (response.code() == 401) {
                            Utils.forceLogout(this@TransferShipToReceiveActivity)  // show dialog before logout
                            return
                        }
                        if (response.isSuccessful) {
                            try {
                                Log.d("TAG", "onResponse: " + response.body().toString())
                                transferList = response.body()!!.getAsJsonArray("data").map {
                                    Gson().fromJson(it, TransferReciveDetail::class.java)
                                } as ArrayList<TransferReciveDetail>
                                filterList = ArrayList(transferList!!)
                                if (filterList!!.isNotEmpty()) {
                                    binding.rvTransferList.layoutManager = LinearLayoutManager(
                                        this@TransferShipToReceiveActivity,
                                        LinearLayoutManager.VERTICAL,
                                        false
                                    )


                                    transferShipToReceiveListAdapter =
                                        TransferShipToReceiveListAdapter(
                                            this@TransferShipToReceiveActivity, filterList!!
                                        ) { allSelected ->

                                        }
                                    binding.rvTransferList.adapter =
                                        transferShipToReceiveListAdapter


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
                                this@TransferShipToReceiveActivity,
                                Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@TransferShipToReceiveActivity,
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