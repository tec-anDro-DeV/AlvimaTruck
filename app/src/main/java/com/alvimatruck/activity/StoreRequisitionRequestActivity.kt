package com.alvimatruck.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityStoreRequisitionRequestBinding
import com.alvimatruck.interfaces.DeleteTransferRequestListener
import com.alvimatruck.model.responses.ItemDetail
import com.alvimatruck.model.responses.LocationDetail
import com.alvimatruck.model.responses.SingleTransfer
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonParser

class StoreRequisitionRequestActivity : BaseActivity<ActivityStoreRequisitionRequestBinding>(),
    DeleteTransferRequestListener {
    var itemList: ArrayList<String> = ArrayList()
    var costCenterList: ArrayList<String> = ArrayList()
    var profitCenterList: ArrayList<String> = ArrayList()
    var inTransitList: ArrayList<String> = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()
    var fromLocationList: ArrayList<String> = ArrayList()
    var selectedItem = ""
    var selectedCostCenter = ""
    var selectedProfitCenter = ""
    var selectedInTransit = ""

    var selectedProduct: ItemDetail? = null

    var selectedLocation: LocationDetail? = null
    var userDetail: UserDetail? = null

    var storeRequisitionRequestItemListAdapter: TransferRequestItemListAdapter? = null

    var requestList: ArrayList<SingleTransfer> = ArrayList()

    var productList: ArrayList<ItemDetail>? = ArrayList()

    var locationList: ArrayList<LocationDetail>? = ArrayList()

    var selectedFromLocation = ""


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
        binding.tvTo.text = "VAN (" + userDetail?.salesPersonCode + ")"

        getItemList()
        getCostCenterList()
        getProfitCenterList()
        getInTransitList()
        getFromLocationList()

        binding.tvItem.setOnClickListener {
            dialogSingleSelection(
                itemList,
                getString(R.string.choose_item),
                getString(R.string.search_item),
                binding.tvItem
            )
        }

        binding.tvCostCenter.setOnClickListener {
            dialogSingleSelection(
                costCenterList,
                getString(R.string.choose_cost_center),
                getString(R.string.search_cost_center),
                binding.tvCostCenter
            )
        }
        binding.tvProfitCenter.setOnClickListener {
            dialogSingleSelection(
                profitCenterList,
                getString(R.string.choose_profit_center),
                getString(R.string.search_profit_center),
                binding.tvProfitCenter
            )
        }

        binding.tvInTransit.setOnClickListener {
            dialogSingleSelection(
                inTransitList,
                getString(R.string.choose_in_transit),
                getString(R.string.search_in_transit),
                binding.tvInTransit
            )
        }

        binding.tvFrom.setOnClickListener {
            dialogSingleSelection(
                fromLocationList,
                getString(R.string.choose_to_location),
                getString(R.string.search_to_location),
                binding.tvTo
            )

        }

        binding.tvAdd.setOnClickListener {
            if (binding.tvItem.text.toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.please_select_item), Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.etQty.text.toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_quantity), Toast.LENGTH_SHORT).show()
            } else {
                val qty = binding.etQty.text.toString().toInt()
                val existingIndex = requestList.indexOfFirst { it.itemNo == selectedProduct!!.no }
                if (existingIndex != -1) {
                    val existingOrder = requestList[existingIndex]
                    existingOrder.quantity = qty
                    storeRequisitionRequestItemListAdapter!!.notifyDataSetChanged()

                } else {
                    val singleRequest = SingleTransfer(
                        selectedItem,
                        selectedProduct!!.no,
                        qty,
                        selectedProduct!!.baseUnitOfMeasure,
                    )
                    requestList.add(singleRequest)
                    storeRequisitionRequestItemListAdapter!!.notifyDataSetChanged()

                }

                binding.llRequestList.visibility = View.VISIBLE
                binding.tvCreateStoreRequisition.visibility = View.VISIBLE
                binding.tvItem.text = ""

                selectedItem = ""

                selectedProduct = null
                binding.etQty.setText("")
                binding.nestedScrollView.post {
                    binding.nestedScrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
        }

        binding.rvTransferList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        storeRequisitionRequestItemListAdapter = TransferRequestItemListAdapter(
            this@StoreRequisitionRequestActivity, requestList, this@StoreRequisitionRequestActivity
        )
        binding.rvTransferList.adapter = storeRequisitionRequestItemListAdapter
    }

    private fun getFromLocationList() {
        val jsonString = SharedHelper.getKey(this, Constants.API_To_Location)
        if (jsonString.isNotEmpty()) {
            locationList =
                JsonParser.parseString(jsonString).asJsonObject.getAsJsonArray("data").map {
                    Gson().fromJson(it, LocationDetail::class.java)
                } as ArrayList<LocationDetail>
            fromLocationList.clear()
            for (item in locationList!!) {
                val name = item.name
                fromLocationList.add(name)
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

            binding.tvFrom -> {
                selectedFromLocation
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

        tvCancel.setOnClickListener { _: View? -> dialog.dismiss() }
        tvConfirm.setOnClickListener { _: View? ->
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
                            if (item.description == singleItemSelectionAdapter.selected) {
                                selectedProduct = item
                            }
                        }
                        val existingOrder = requestList.find { it.itemNo == selectedProduct?.no }
                        if (existingOrder != null) {
                            binding.etQty.setText(existingOrder.quantity.toString())
                        } else {
                            binding.etQty.setText("")
                        }
                    }


                    binding.tvFrom -> {
                        selectedFromLocation = singleItemSelectionAdapter.selected
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
        val jsonString = SharedHelper.getKey(this, Constants.API_Item_List)
        if (jsonString.isNotEmpty()) {
            productList =
                JsonParser.parseString(jsonString).asJsonObject.getAsJsonArray("data").map {
                    Gson().fromJson(it, ItemDetail::class.java)
                } as ArrayList<ItemDetail>
            itemList.clear()
            for (item in productList!!) {
                val code = item.description
                itemList.add(code)
            }
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
                val code = item.asJsonObject.get("value")?.asString
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
        storeRequisitionRequestItemListAdapter!!.notifyDataSetChanged()
        if (requestList.isEmpty()) {
            binding.llRequestList.visibility = View.GONE
            binding.tvCreateStoreRequisition.visibility = View.GONE
        }
    }

}