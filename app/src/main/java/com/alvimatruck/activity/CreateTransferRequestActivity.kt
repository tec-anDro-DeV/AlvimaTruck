package com.alvimatruck.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.adapter.SingleItemSelectionAdapter
import com.alvimatruck.adapter.TransferRequestItemListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityCreateTransferRequestBinding
import com.alvimatruck.interfaces.DeleteRequestListener
import com.alvimatruck.model.responses.ItemDetail
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonParser

class CreateTransferRequestActivity : BaseActivity<ActivityCreateTransferRequestBinding>(),
    DeleteRequestListener {
    var itemList: ArrayList<String> = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()

    var costCenterList: ArrayList<String> = ArrayList()
    var profitCenterList: ArrayList<String> = ArrayList()
    var inTransitList: ArrayList<String> = ArrayList()

    var selectedProduct: ItemDetail? = null
    var userDetail: UserDetail? = null

    var transferRequestItemListAdapter: TransferRequestItemListAdapter? = null

    var requestList: ArrayList<String> = ArrayList()

    var productList: ArrayList<ItemDetail>? = ArrayList()


    var selectedItem = ""
    var selectedCostCenter = ""
    var selectedProfitCenter = ""
    var selectedInTransit = ""

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
        //  binding.tvTransferNumber.text = System.currentTimeMillis().toString()

        getItemList()
        getCostCenterList()
        getProfitCenterList()
        getInTransitList()

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

        binding.tvAdd.setOnClickListener {
            requestList.add("")
            binding.llRequestList.visibility = View.VISIBLE
            binding.tvCreateTransferRequest.visibility = View.VISIBLE
            binding.tvItem.text = ""
            binding.tvCostCenter.text = ""
            binding.tvProfitCenter.text = ""
            binding.tvInTransit.text = ""
            selectedItem = ""
            selectedCostCenter = ""
            selectedProfitCenter = ""
            selectedInTransit = ""
            selectedProduct = null
            transferRequestItemListAdapter!!.notifyDataSetChanged()
            binding.nestedScrollView.post {
                binding.nestedScrollView.fullScroll(View.FOCUS_DOWN)
            }
        }

        binding.rvTransferList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        transferRequestItemListAdapter = TransferRequestItemListAdapter(
            this@CreateTransferRequestActivity, requestList, this@CreateTransferRequestActivity
        )
        binding.rvTransferList.adapter = transferRequestItemListAdapter

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
        val jsonString = SharedHelper.getKey(this, Constants.API_Item_List)
        if (jsonString.isNotEmpty()) {
            productList = JsonParser.parseString(jsonString).asJsonArray.map {
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

    override fun onDeleteOrder(orderDetail: String) {
        requestList.remove(orderDetail)
        transferRequestItemListAdapter!!.notifyDataSetChanged()
        if (requestList.isEmpty()) {
            binding.llRequestList.visibility = View.GONE
            binding.tvCreateTransferRequest.visibility = View.GONE
        }

    }
}