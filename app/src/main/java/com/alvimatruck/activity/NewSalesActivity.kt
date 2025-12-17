package com.alvimatruck.activity

import android.content.Intent
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
import com.alvimatruck.adapter.NewSalesItemListAdapter
import com.alvimatruck.adapter.SingleItemSelectionAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityNewSalesBinding
import com.alvimatruck.interfaces.DeleteOrderListener
import com.alvimatruck.model.request.NewOrderRequest
import com.alvimatruck.model.responses.CustomerDetail
import com.alvimatruck.model.responses.ItemDetail
import com.alvimatruck.model.responses.SingleOrder
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.alvimatruck.utils.Utils.to2Decimal
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewSalesActivity : BaseActivity<ActivityNewSalesBinding>(), DeleteOrderListener {
    var locationCodeList: ArrayList<String>? = ArrayList()
    var paymentCodeList: ArrayList<String>? = ArrayList()
    var itemList: ArrayList<String>? = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()
    var selectedLocationCode = ""
    var selectedPaymentCode = ""
    var selectedItem = ""
    var selectedProduct: ItemDetail? = null
    var customerDetail: CustomerDetail? = null
    var userDetail: UserDetail? = null

    var minQty = 0
    var tempVat = 0.0
    var tempUnitPrice = 0.0

    var newSalesItemListAdapter: NewSalesItemListAdapter? = null

    var orderList: ArrayList<SingleOrder> = ArrayList()

    var productList: ArrayList<ItemDetail>? = ArrayList()


    override fun inflateBinding(): ActivityNewSalesBinding {
        return ActivityNewSalesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        userDetail =
            Gson().fromJson(SharedHelper.getKey(this, Constants.UserDetail), UserDetail::class.java)

        if (intent != null) {
            customerDetail = Gson().fromJson(
                intent.getStringExtra(Constants.CustomerDetail).toString(),
                CustomerDetail::class.java
            )
            binding.tvCustomer.text = customerDetail!!.searchName
            val number = if (!customerDetail?.getFormattedContactNo().isNullOrBlank()) {
                customerDetail?.getFormattedContactNo()
            } else {
                customerDetail?.getFormattedTelephoneNo()
            }

            binding.tvTelephoneNumber.text = number ?: ""
            binding.tvSalesperson.text = userDetail?.firstName + " " + userDetail?.lastName
        }

        binding.tvPostingDate.text = Utils.getFullDateWithTime(System.currentTimeMillis())
        binding.tvOrderDate.text = Utils.getFullDate(System.currentTimeMillis())
        //     binding.tvToken.text = System.currentTimeMillis().toString()

        getLocationCodeList()
        getPaymentCodeList()
        getItemList()

        binding.tvLocationCode.setOnClickListener {
            dialogSingleSelection(
                locationCodeList!!,
                "Choose Location Code",
                "Search Location Code",
                binding.tvLocationCode
            )
        }

        binding.tvPaymentCode.setOnClickListener {
            dialogSingleSelection(
                paymentCodeList!!,
                "Choose Payment Code",
                "Search Payment Code",
                binding.tvPaymentCode
            )
        }

        binding.tvItem.setOnClickListener {
            dialogSingleSelection(
                itemList!!, "Choose Item", "Search Item", binding.tvItem
            )
        }

        binding.tvCancel.setOnClickListener {
            handleBackPressed()
        }

        binding.tvConfirmOrder.setOnClickListener {
            if (binding.tvLocationCode.text.toString().isEmpty()) {
                Toast.makeText(this, "Select Location Code", Toast.LENGTH_SHORT).show()
            } else if (binding.tvPaymentCode.text.toString().isEmpty()) {
                Toast.makeText(this, "Select Payment Code", Toast.LENGTH_SHORT).show()
            } else {
                newOrderAPI()
            }
        }

        binding.tvAdd.setOnClickListener {
            if (binding.tvItem.text.toString().isEmpty()) {
                Toast.makeText(this, "Please select Item", Toast.LENGTH_SHORT).show()
            } else if (binding.etSalesPrice.text.toString().isEmpty()) {
                Toast.makeText(this, "Enter Sales Price", Toast.LENGTH_SHORT).show()
            } else if (binding.etQuantity.text.toString().isEmpty()) {
                Toast.makeText(this, "Enter Quantity", Toast.LENGTH_SHORT).show()
            } else if (binding.etQuantity.text.toString().toInt() < minQty) {
                Toast.makeText(
                    this,
                    "Quantity must be greater than or equal to $minQty",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Get current values
                tempUnitPrice = binding.etSalesPrice.text.toString().toDouble()
                val qty = binding.etQuantity.text.toString().toInt()
                val finalTotal = (tempUnitPrice + tempVat) * qty.toDouble()

                // Check if item exists in the list
                // Assumes 'itemNo' is the property holding the product ID in SingleOrder
                val existingIndex = orderList.indexOfFirst { it.itemNo == selectedProduct!!.no }

                if (existingIndex != -1) {
                    // --- UPDATE EXISTING ORDER ---
                    val existingOrder = orderList[existingIndex]
                    existingOrder.quantity = qty
                    existingOrder.unitPrice = tempUnitPrice
                    existingOrder.finalPrice = finalTotal
                    existingOrder.vat = tempVat // Update VAT in case it changed

                    newSalesItemListAdapter!!.notifyItemChanged(existingIndex)
                } else {
                    // --- ADD NEW ORDER ---
                    val singleOrder = SingleOrder(
                        finalTotal,
                        selectedItem,
                        selectedProduct!!.no,
                        qty,
                        tempUnitPrice,
                        tempVat,
                        selectedProduct!!.baseUnitOfMeasure
                    )
                    orderList.add(singleOrder)
                    newSalesItemListAdapter!!.notifyDataSetChanged() // Or notifyItemInserted
                }

                // Reset UI
                binding.llOrderList.visibility = View.VISIBLE
                binding.llBottomButtons.visibility = View.VISIBLE
                binding.llOrderTotal.visibility = View.VISIBLE

                binding.tvItem.text = ""
                selectedItem = ""
                selectedProduct = null
                minQty = 0
                binding.etQuantity.setText("")
                binding.etSalesPrice.setText("")
                tempUnitPrice = 0.0
                tempVat = 0.0

                binding.nestedScrollView.post {
                    binding.nestedScrollView.fullScroll(View.FOCUS_DOWN)
                }
                calculateFinalTotal()
            }

        }



        binding.rvProducts.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        newSalesItemListAdapter = NewSalesItemListAdapter(
            this@NewSalesActivity, orderList, this@NewSalesActivity
        )
        binding.rvProducts.adapter = newSalesItemListAdapter


    }

    private fun newOrderAPI() {

        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@NewSalesActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.newOrder(
                NewOrderRequest(
                    binding.tvCustomer.text.toString(),
                    customerDetail!!.no,
                    binding.tvTotal.text.toString().replace("ETB", "").toDouble(),
                    orderList,
                    selectedLocationCode,
                    selectedPaymentCode,
                    binding.tvSubTotal.text.toString().replace("ETB", "").toDouble(),
                    binding.tvVat.text.toString().replace("+ ETB", "").toDouble()
                )
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    ProgressDialog.dismiss()
                    if (response.code() == 401) {
                        Utils.forceLogout(this@NewSalesActivity)  // show dialog before logout
                        return
                    }
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            Toast.makeText(
                                this@NewSalesActivity,
                                response.body()!!.get("message").toString().replace('"', ' ')
                                    .trim(),
                                Toast.LENGTH_SHORT
                            ).show()
                            customerDetail!!.visitedToday = true
                            val intent = Intent()
                            intent.putExtra(Constants.CustomerDetail, Gson().toJson(customerDetail))
                            setResult(RESULT_OK, intent)
                            finish()

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@NewSalesActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@NewSalesActivity,
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

    fun calculateFinalTotal() {
        var subtotal = 0.0
        var vat = 0.0
        var total = 0.0
        for (item in orderList) {
            subtotal += (item.unitPrice * item.quantity)
            vat += (item.vat * item.quantity)
            total += item.finalPrice
        }
        binding.tvSubTotal.text = "ETB " + subtotal.to2Decimal()
        binding.tvVat.text = "+ ETB " + vat.to2Decimal()
        binding.tvTotal.text = "ETB " + total.to2Decimal()

    }

    private fun dialogSingleSelection(
        list: ArrayList<String>, title: String, hint: String, textView: TextView
    ) {
        filterList!!.clear()
        filterList!!.addAll(list)
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_single_selection, null)
        val selectedGroup: String = when (textView) {
            binding.tvLocationCode -> {
                selectedLocationCode
            }

            binding.tvItem -> {
                selectedItem
            }

            else -> {
                selectedPaymentCode
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
                binding.tvLocationCode -> {
                    selectedLocationCode = singleItemSelectionAdapter.selected
                }

                binding.tvItem -> {
                    selectedItem = singleItemSelectionAdapter.selected
                    for (item in productList!!) {
                        if (item.description == singleItemSelectionAdapter.selected) {
                            selectedProduct = item
                        }
                    }
                    // Check if item exists in orderList
                    val existingOrder = orderList.find { it.itemNo == selectedProduct?.no }
                    customerPriceAPI(existingOrder)
                }

                else -> {
                    selectedPaymentCode = singleItemSelectionAdapter.selected
                }
            }
            textView.text = singleItemSelectionAdapter.selected
            dialog.dismiss()
        }
    }

    private fun customerPriceAPI(existingOrder: SingleOrder? = null) {
        // Only clear fields if this is a NEW item
        if (existingOrder == null) {
            binding.etQuantity.setText("")
            binding.etSalesPrice.setText("")
        }
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@NewSalesActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.customerPrice(customerDetail!!.customerPriceGroup, selectedProduct!!.no)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        ProgressDialog.dismiss()
                        if (response.code() == 204) {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            minQty = 1
                            tempVat = 0.0

                            // API says price is not fixed -> Enable editing
                            binding.etSalesPrice.isEnabled = true

                            if (existingOrder != null) {
                                // Restore User's values from Order List
                                binding.etQuantity.setText(existingOrder.quantity.toString())
                                binding.etSalesPrice.setText(existingOrder.unitPrice.toString())
                                tempUnitPrice = existingOrder.unitPrice
                            } else {
                                binding.etQuantity.setText(minQty.toString())
                            }
                            return
                        }
                        if (response.isSuccessful) {
                            try {
                                Log.d("TAG", "onCustomerItemPrice: " + response.body().toString())

                                if (response.body() != null) {
                                    minQty =
                                        response.body()!!.asJsonObject.get("minimumQuantity").asInt
                                    binding.etSalesPrice.isEnabled = false
                                    tempUnitPrice =
                                        response.body()!!.asJsonObject.get("unitPrice").asDouble
                                    tempVat =
                                        response.body()!!.asJsonObject.get("unitPriceInclVAT").asDouble
                                    val finalPrice = tempUnitPrice + tempVat
                                    binding.etSalesPrice.setText(finalPrice.toString())

                                    if (existingOrder != null) {
                                        binding.etQuantity.setText(existingOrder.quantity.toString())
                                    } else {
                                        binding.etQuantity.setText(minQty.toString())
                                    }


                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Toast.makeText(
                                this@NewSalesActivity,
                                Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@NewSalesActivity,
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

    private fun getLocationCodeList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@NewSalesActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.locationCodeList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            if (response.body() != null && response.body()!!.has("data")) {
                                val dataArray = response.body()!!.getAsJsonArray("data")

                                for (item in dataArray) {
                                    val obj = item.asJsonObject
                                    val code = obj.get("code").asString
                                    locationCodeList!!.add(code)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@NewSalesActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@NewSalesActivity,
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

    private fun getPaymentCodeList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@NewSalesActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.paymentCodeList().enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    ProgressDialog.dismiss()
                    if (response.isSuccessful) {
                        try {
                            Log.d("TAG", "onResponse: " + response.body().toString())
                            if (response.body() != null && response.body()!!.has("data")) {
                                val dataArray = response.body()!!.getAsJsonArray("data")

                                for (item in dataArray) {
                                    val obj = item.asJsonObject
                                    val code = obj.get("code").asString
                                    paymentCodeList!!.add(code)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this@NewSalesActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Toast.makeText(
                        this@NewSalesActivity,
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

    private fun getItemList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@NewSalesActivity)
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
                            this@NewSalesActivity,
                            Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<JsonArray?>, t: Throwable) {
                    Toast.makeText(
                        this@NewSalesActivity,
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


    override fun onDeleteOrder(orderDetail: SingleOrder) {
        orderList.remove(orderDetail)
        newSalesItemListAdapter!!.notifyDataSetChanged()
        if (orderList.isEmpty()) {
            binding.llOrderList.visibility = View.GONE
            binding.llBottomButtons.visibility = View.GONE
            binding.llOrderTotal.visibility = View.GONE
        } else {
            calculateFinalTotal()
        }
    }


}