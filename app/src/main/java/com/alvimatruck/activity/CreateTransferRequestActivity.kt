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
import com.alvimatruck.adapter.TransferRequestItemListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityCreateTransferRequestBinding
import com.alvimatruck.interfaces.DeleteTransferRequestListener
import com.alvimatruck.model.request.TransferRequest
import com.alvimatruck.model.responses.LocationDetail
import com.alvimatruck.model.responses.SingleTransfer
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.model.responses.VanStockDetail
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

class CreateTransferRequestActivity : BaseActivity<ActivityCreateTransferRequestBinding>(),
    DeleteTransferRequestListener {
    var itemList: ArrayList<String> = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()

    var costCenterList: ArrayList<String> = ArrayList()
    var profitCenterList: ArrayList<String> = ArrayList()
    var inTransitList: ArrayList<String> = ArrayList()
    var toLocationList: ArrayList<String> = ArrayList()

    var selectedProduct: VanStockDetail? = null
    var selectedLocation: LocationDetail? = null
    var userDetail: UserDetail? = null

    var transferRequestItemListAdapter: TransferRequestItemListAdapter? = null

    var requestList: ArrayList<SingleTransfer> = ArrayList()

    var productList: ArrayList<VanStockDetail>? = ArrayList()
    var locationList: ArrayList<LocationDetail>? = ArrayList()


    var selectedItem = ""
    var selectedCostCenter = ""
    var selectedProfitCenter = ""
    var selectedInTransit = ""
    var selectedToLocation = ""


    override fun inflateBinding(): ActivityCreateTransferRequestBinding {
        return ActivityCreateTransferRequestBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        userDetail =
            Gson().fromJson(SharedHelper.getKey(this, Constants.UserDetail), UserDetail::class.java)

        binding.tvDateTime.text = Utils.getFullDateWithTime(System.currentTimeMillis())
        binding.tvFrom.text = "VAN (" + userDetail?.salesPersonCode + ")"


        getItemList()
        getCostCenterList()
        getProfitCenterList()
        getInTransitList()
        getToLocationList()

        binding.tvItem.setOnClickListener {
            dialogSingleSelection(
                itemList, "Choose Item", "Search Item", binding.tvItem
            )
        }
        binding.tvCostCenter.setOnClickListener {
            dialogSingleSelection(
                costCenterList, "Choose Cost Center", "Search Cost Center", binding.tvCostCenter
            )
        }

        binding.tvProfitCenter.setOnClickListener {
            dialogSingleSelection(
                profitCenterList,
                "Choose Profit Center",
                "Search Profit Center",
                binding.tvProfitCenter
            )
        }

        binding.tvInTransit.setOnClickListener {
            dialogSingleSelection(
                inTransitList, "Choose In Transit", "Search In Transit", binding.tvInTransit
            )
        }

        binding.tvTo.setOnClickListener {
            dialogSingleSelection(
                toLocationList, "Choose To Location", "Search To Location", binding.tvTo
            )

        }

        binding.tvAdd.setOnClickListener {
            if (binding.tvItem.text.toString().isEmpty()) {
                Toast.makeText(this, "Please select Item", Toast.LENGTH_SHORT).show()
            } else if (binding.etQuantity.text.toString().isEmpty()) {
                Toast.makeText(this, "Enter Quantity", Toast.LENGTH_SHORT).show()
            } else if (binding.etQuantity.text.toString().toInt() > selectedProduct!!.qtyOnHand) {
                Toast.makeText(
                    this,
                    "Only up to ${selectedProduct!!.qtyOnHand} units are available for transfer",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val qty = binding.etQuantity.text.toString().toInt()
                val existingIndex =
                    requestList.indexOfFirst { it.itemNo == selectedProduct!!.itemNo }
                if (existingIndex != -1) {
                    val existingOrder = requestList[existingIndex]
                    existingOrder.quantity = qty
                    transferRequestItemListAdapter!!.notifyDataSetChanged()

                } else {
                    val singleRequest = SingleTransfer(
                        selectedItem,
                        selectedProduct!!.itemNo,
                        qty,
                        selectedProduct!!.unitOfMeasure,
                    )
                    requestList.add(singleRequest)
                    transferRequestItemListAdapter!!.notifyDataSetChanged()

                }

                binding.llRequestList.visibility = View.VISIBLE
                binding.tvCreateTransferRequest.visibility = View.VISIBLE
                binding.tvItem.text = ""

                selectedItem = ""

                selectedProduct = null
                binding.etQuantity.setText("")
                binding.nestedScrollView.post {
                    binding.nestedScrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
        }

        binding.rvTransferList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        transferRequestItemListAdapter = TransferRequestItemListAdapter(
            this@CreateTransferRequestActivity, requestList, this@CreateTransferRequestActivity
        )
        binding.rvTransferList.adapter = transferRequestItemListAdapter

        binding.tvCreateTransferRequest.setOnClickListener {
            if (binding.tvProfitCenter.text.toString().isEmpty()) {
                Toast.makeText(this, "Please select Profit Center", Toast.LENGTH_SHORT).show()
            } else if (binding.tvCostCenter.text.toString().isEmpty()) {
                Toast.makeText(this, "Please select Cost Center", Toast.LENGTH_SHORT).show()
            } else if (binding.tvTo.text.toString().isEmpty()) {
                Toast.makeText(this, "Please select To Location", Toast.LENGTH_SHORT).show()
            } else if (binding.tvInTransit.text.toString().isEmpty()) {
                Toast.makeText(this, "Please select In Transit", Toast.LENGTH_SHORT).show()
            } else {
                transferRequestAPI()
            }
        }

    }

    private fun transferRequestAPI() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@CreateTransferRequestActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.newTransferRequest(
                TransferRequest(
                    selectedCostCenter,
                    selectedInTransit,
                    requestList,
                    selectedProfitCenter,
                    userDetail?.salesPersonCode!!,
                    selectedLocation!!.code
                )
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@CreateTransferRequestActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@CreateTransferRequestActivity,
                                response.body()!!.get("message").toString().replace('"', ' ')
                                    .trim(),
                                Toast.LENGTH_SHORT
                            ).show()

                            handleBackPressed()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@CreateTransferRequestActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@CreateTransferRequestActivity,
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

    private fun getToLocationList() {
        val jsonString = SharedHelper.getKey(this, Constants.API_To_Location)
        if (jsonString.isNotEmpty()) {
            locationList =
                JsonParser.parseString(jsonString).asJsonObject.getAsJsonArray("data").map {
                    Gson().fromJson(it, LocationDetail::class.java)
                } as ArrayList<LocationDetail>
            toLocationList.clear()
            for (item in locationList!!) {
                val name = item.name
                toLocationList.add(name)
            }
        }

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

            binding.tvTo -> {
                selectedToLocation
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
            if (filterList!!.isNotEmpty()) {
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
                            if (item.itemName == singleItemSelectionAdapter.selected) {
                                selectedProduct = item
                            }
                        }
                        val existingOrder =
                            requestList.find { it.itemNo == selectedProduct?.itemNo }
                        if (existingOrder != null) {
                            binding.etQuantity.setText(existingOrder.quantity.toString())
                        } else {
                            binding.etQuantity.setText("")
                        }
                    }


                    binding.tvTo -> {
                        selectedToLocation = singleItemSelectionAdapter.selected
                        for (item in locationList!!) {
                            if (item.name == singleItemSelectionAdapter.selected) {
                                selectedLocation = item
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
    }

    private fun getItemList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@CreateTransferRequestActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.vanStock(userDetail?.salesPersonCode!!)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        ProgressDialog.dismiss()
                        if (response.isSuccessful) {
                            try {
                                Log.d("TAG", "onResponse Item: " + response.body().toString())
                                productList = response.body()!!.getAsJsonArray("data").map {
                                    Gson().fromJson(it, VanStockDetail::class.java)
                                } as ArrayList<VanStockDetail>
                                itemList.clear()
                                for (item in productList!!) {
                                    val code = item.itemName
                                    itemList.add(code)
                                }


                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                this@CreateTransferRequestActivity,
                                Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@CreateTransferRequestActivity,
                            getString(R.string.api_fail_message),
                            Toast.LENGTH_SHORT
                        ).show()
                        ProgressDialog.dismiss()
                    }
                })
        } else {
            Toast.makeText(
                this,
                getString(R.string.please_check_your_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getCostCenterList() {
        costCenterList.clear()

        val jsonString = SharedHelper.getKey(this, Constants.API_CostCenter_Code)

        if (jsonString.isNotEmpty()) {
            val dataArray = JsonParser.parseString(jsonString).asJsonObject.getAsJsonArray("data")

            for (item in dataArray) {
                val code = item.asJsonObject.get("code")?.asString
                if (!code.isNullOrEmpty()) {
                    costCenterList.add(code)
                }
            }
        }
    }

    private fun getProfitCenterList() {
        profitCenterList.clear()

        val jsonString = SharedHelper.getKey(this, Constants.API_ProfitCenter_Code)

        if (jsonString.isNotEmpty()) {
            val dataArray = JsonParser.parseString(jsonString).asJsonObject.getAsJsonArray("data")

            for (item in dataArray) {
                val code = item.asJsonObject.get("code")?.asString
                if (!code.isNullOrEmpty()) {
                    profitCenterList.add(code)
                }
            }
        }
    }

    private fun getInTransitList() {
        inTransitList.clear()

        val jsonString = SharedHelper.getKey(this, Constants.API_Intransit_Code)

        if (jsonString.isNotEmpty()) {
            val dataArray = JsonParser.parseString(jsonString).asJsonObject.getAsJsonArray("data")

            for (item in dataArray) {
                val code = item.asJsonObject.get("code")?.asString
                if (!code.isNullOrEmpty()) {
                    inTransitList.add(code)
                }
            }
        }
    }

    override fun onDeleteRequest(request: SingleTransfer) {
        requestList.remove(request)
        transferRequestItemListAdapter!!.notifyDataSetChanged()
        if (requestList.isEmpty()) {
            binding.llRequestList.visibility = View.GONE
            binding.tvCreateTransferRequest.visibility = View.GONE
        }
    }
}