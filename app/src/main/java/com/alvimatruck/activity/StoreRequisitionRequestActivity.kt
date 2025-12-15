package com.alvimatruck.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.adapter.SingleItemSelectionAdapter
import com.alvimatruck.adapter.StoreRequisitionRequestItemListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityStoreRequisitionRequestBinding
import com.alvimatruck.interfaces.DeleteRequestListener
import com.alvimatruck.model.responses.ItemDetail
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoreRequisitionRequestActivity : BaseActivity<ActivityStoreRequisitionRequestBinding>(),
    DeleteRequestListener {
    var itemList: ArrayList<String>? = ArrayList()
    var costCenterList: ArrayList<String>? = ArrayList()
    var profitCenterList: ArrayList<String>? = ArrayList()
    var inTransitList: ArrayList<String>? = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()
    var selectedItem = ""
    var selectedCostCenter = ""
    var selectedProfitCenter = ""
    var selectedInTransit = ""

    var selectedProduct: ItemDetail? = null
    var userDetail: UserDetail? = null

    var storeRequisitionRequestItemListAdapter: StoreRequisitionRequestItemListAdapter? = null

    var requestList: ArrayList<String> = ArrayList()

    var productList: ArrayList<ItemDetail>? = ArrayList()

    override fun inflateBinding(): ActivityStoreRequisitionRequestBinding {
        return ActivityStoreRequisitionRequestBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        binding.tvDateTime.text = Utils.getFullDateWithTime(System.currentTimeMillis())
        //  binding.tvTransferNumber.text = System.currentTimeMillis().toString()

        userDetail =
            Gson().fromJson(SharedHelper.getKey(this, Constants.UserDetail), UserDetail::class.java)

        getItemList()
        getCostCenterList()
        getProfitCenterList()
        getInTransitList()

        binding.tvItem.setOnClickListener {
            dialogSingleSelection(
                itemList!!, "Choose Item", "Search Item", binding.tvItem
            )
        }

        binding.tvCostCenter.setOnClickListener {
            dialogSingleSelection(
                costCenterList!!, "Choose Cost Center", "Search Cost Center", binding.tvCostCenter
            )
        }

        binding.tvProfitCenter.setOnClickListener {
            dialogSingleSelection(
                profitCenterList!!,
                "Choose Profit Center",
                "Search Profit Center",
                binding.tvProfitCenter
            )
        }

        binding.tvInTransit.setOnClickListener {
            dialogSingleSelection(
                inTransitList!!, "Choose In Transit", "Search In Transit", binding.tvInTransit
            )
        }

        binding.tvAdd.setOnClickListener {
            requestList.add("")
            binding.llRequestList.visibility = View.VISIBLE
            binding.tvCreateStoreRequisition.visibility = View.VISIBLE
            binding.tvItem.text = ""
            binding.tvCostCenter.text = ""
            binding.tvProfitCenter.text = ""
            binding.tvInTransit.text = ""
            selectedItem = ""
            selectedCostCenter = ""
            selectedProfitCenter = ""
            selectedInTransit = ""
            selectedProduct = null
            storeRequisitionRequestItemListAdapter!!.notifyDataSetChanged()
            binding.nestedScrollView.post {
                binding.nestedScrollView.fullScroll(View.FOCUS_DOWN)
            }
        }

        binding.rvTransferList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        storeRequisitionRequestItemListAdapter = StoreRequisitionRequestItemListAdapter(
            this@StoreRequisitionRequestActivity, requestList, this@StoreRequisitionRequestActivity
        )
        binding.rvTransferList.adapter = storeRequisitionRequestItemListAdapter
    }

    private fun dialogSingleSelection(
        list: ArrayList<String>, title: String, hint: String, textView: TextView
    ) {
        filterList!!.clear()
        filterList!!.addAll(list)
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_single_selection, null)
        val selectedGroup: String = when (textView) {
            binding.tvCostCenter -> {
                selectedCostCenter
            }

            binding.tvProfitCenter -> {
                selectedProfitCenter
            }

            binding.tvItem -> {
                selectedItem
            }

            else -> {
                selectedInTransit
            }
        }
        val singleItemSelectionAdapter =
            SingleItemSelectionAdapter(this, filterList!!, selectedGroup)

        val lLayout = LinearLayoutManager(this)
        val rvBinList = alertLayout.findViewById<RecyclerView>(R.id.rvItemList)
        rvBinList.layoutManager = lLayout
        rvBinList.adapter = singleItemSelectionAdapter
        val etBinSearch = alertLayout.findViewById<EditText>(R.id.etItemSearch)
        etBinSearch.hint = hint



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
                singleItemSelectionAdapter.notifyDataSetChanged()
            }
        })

        val tvCancel = alertLayout.findViewById<TextView>(R.id.tvCancel2)
        val tvConfirm = alertLayout.findViewById<TextView>(R.id.tvConfirm2)
        val tvTitle = alertLayout.findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = title


        val alert = AlertDialog.Builder(this)
        alert.setView(alertLayout)
        alert.setCancelable(false)

        val dialog = alert.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        dialog.show()

        val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        tvCancel.setOnClickListener { view: View? -> dialog.dismiss() }
        tvConfirm.setOnClickListener { view: View? ->
            when (textView) {
                binding.tvCostCenter -> {
                    selectedCostCenter = singleItemSelectionAdapter.selected
                }

                binding.tvProfitCenter -> {
                    selectedProfitCenter = singleItemSelectionAdapter.selected
                }

                binding.tvItem -> {
                    selectedItem = singleItemSelectionAdapter.selected
                    for (item in productList!!) {
                        if (item.description == singleItemSelectionAdapter.selected) {
                            selectedProduct = item
                        }
                    }
                }

                else -> {
                    selectedInTransit = singleItemSelectionAdapter.selected
                }
            }
            textView.text = singleItemSelectionAdapter.selected
            dialog.dismiss()
        }
    }

    private fun getItemList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@StoreRequisitionRequestActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.itemList().enqueue(object : Callback<JsonArray> {
                override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse Item: " + response.body().toString())
                            if (response.body() != null) {
                                productList = response.body()!!.getAsJsonArray().map {
                                    Gson().fromJson(it, ItemDetail::class.java)
                                } as ArrayList<ItemDetail>
                                for (item in productList!!) {
                                    val code = item.description
                                    itemList!!.add(code)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@StoreRequisitionRequestActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonArray?>, t: Throwable) {
                    Toast.makeText(
                        this@StoreRequisitionRequestActivity,
                        getString(R.string.api_fail_message),
                        Toast.LENGTH_SHORT
                    ).show()
                    ProgressDialog.dismiss()
                }
            })
        } else {
            Toast.makeText(
                this, getString(R.string.please_check_your_internet_connection), Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun getCostCenterList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@StoreRequisitionRequestActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.costCenterCodeList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            if (response.body() != null && response.body()!!.has("data")) {
                                val dataArray = response.body()!!.getAsJsonArray("data")

                                for (item in dataArray) {
                                    val obj = item.asJsonObject
                                    val code = obj.get("code").asString
                                    costCenterList!!.add(code)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@StoreRequisitionRequestActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@StoreRequisitionRequestActivity,
                        getString(R.string.api_fail_message),
                        Toast.LENGTH_SHORT
                    ).show()
                    ProgressDialog.dismiss()
                }
            })
        } else {
            Toast.makeText(
                this, getString(R.string.please_check_your_internet_connection), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getProfitCenterList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@StoreRequisitionRequestActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.profitCenterCodeList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            if (response.body() != null && response.body()!!.has("data")) {
                                val dataArray = response.body()!!.getAsJsonArray("data")

                                for (item in dataArray) {
                                    val obj = item.asJsonObject
                                    val code = obj.get("code").asString
                                    profitCenterList!!.add(code)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@StoreRequisitionRequestActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@StoreRequisitionRequestActivity,
                        getString(R.string.api_fail_message),
                        Toast.LENGTH_SHORT
                    ).show()
                    ProgressDialog.dismiss()
                }
            })
        } else {
            Toast.makeText(
                this, getString(R.string.please_check_your_internet_connection), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getInTransitList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@StoreRequisitionRequestActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.inTransitCodeList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            if (response.body() != null && response.body()!!.has("data")) {
                                val dataArray = response.body()!!.getAsJsonArray("data")

                                for (item in dataArray) {
                                    val obj = item.asJsonObject
                                    val code = obj.get("code").asString
                                    inTransitList!!.add(code)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@StoreRequisitionRequestActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@StoreRequisitionRequestActivity,
                        getString(R.string.api_fail_message),
                        Toast.LENGTH_SHORT
                    ).show()
                    ProgressDialog.dismiss()
                }
            })
        } else {
            Toast.makeText(
                this, getString(R.string.please_check_your_internet_connection), Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDeleteOrder(orderDetail: String) {

        requestList.remove(orderDetail)
        storeRequisitionRequestItemListAdapter!!.notifyDataSetChanged()
        if (requestList.isEmpty()) {
            binding.llRequestList.visibility = View.GONE
            binding.tvCreateStoreRequisition.visibility = View.GONE
        }
    }

}