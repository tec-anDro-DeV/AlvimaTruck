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
import com.alvimatruck.model.responses.SingleOrder
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.model.responses.VanStockDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.alvimatruck.utils.Utils.to2Decimal
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewSalesActivity : BaseActivity<ActivityNewSalesBinding>(), DeleteOrderListener {
    var itemList: ArrayList<String> = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()

    // var selectedLocationCode = ""
    // var selectedPaymentCode = ""
    var selectedItem = ""
    var selectedProduct: VanStockDetail? = null
    var customerDetail: CustomerDetail? = null
    var userDetail: UserDetail? = null

    var minQty = 0
    var tempVat = 0.0
    var tempUnitPrice = 0.0

    var newSalesItemListAdapter: NewSalesItemListAdapter? = null

    var orderList: ArrayList<SingleOrder> = ArrayList()

    var productList: ArrayList<VanStockDetail>? = ArrayList()


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
            binding.tvSalesperson.text = userDetail?.driverFullName
            binding.tvLocationCode.text = userDetail?.salesPersonCode
            // binding.tvPaymentCode.text = userDetail?.salesPersonCode
        }

        binding.tvPostingDate.text = Utils.getFullDateWithTime(System.currentTimeMillis())
        binding.tvOrderDate.text = Utils.getFullDate(System.currentTimeMillis())
        //     binding.tvToken.text = System.currentTimeMillis().toString()

        getItemList()


        binding.tvItem.setOnClickListener {
            dialogSingleSelection()
        }

        binding.tvCancel.setOnClickListener {
            handleBackPressed()
        }

        binding.tvConfirmOrder.setOnClickListener {
            newOrderAPI()
        }

        binding.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) return
                if (s.toString().toInt() > selectedProduct!!.qtyOnHand) {
                    Toast.makeText(
                        this@NewSalesActivity,
                        "Only up to ${selectedProduct!!.qtyOnHand} units are available in the van ",
                        Toast.LENGTH_SHORT
                    ).show()
                    s.delete(s.length - 1, s.length)
                }
            }
        })



        binding.tvAdd.setOnClickListener {
            if (binding.tvItem.text.toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.please_select_item), Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.etSalesPrice.text.toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_sales_price), Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.etQuantity.text.toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_quantity), Toast.LENGTH_SHORT).show()
            } else if (binding.etQuantity.text.toString().toInt() < minQty) {
                Toast.makeText(
                    this,
                    getString(R.string.quantity_must_be_greater_than_or_equal_to, minQty),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.etQuantity.text.toString().toInt() > selectedProduct!!.qtyOnHand) {
                Toast.makeText(
                    this, getString(
                        R.string.only_up_to_units_are_available_in_the_van,
                        selectedProduct!!.qtyOnHand
                    ), Toast.LENGTH_SHORT
                ).show()
            } else {
                // Get current values
                if (tempVat == 0.0) {
                    tempUnitPrice = binding.etSalesPrice.text.toString().toDouble()
                }
                val qty = binding.etQuantity.text.toString().toInt()
                val finalTotal = (tempUnitPrice + tempVat) * qty.toDouble()

                // Check if item exists in the list
                // Assumes 'itemNo' is the property holding the product ID in SingleOrder
                val existingIndex = orderList.indexOfFirst { it.itemNo == selectedProduct!!.itemNo }

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
                        selectedProduct!!.itemNo,
                        qty,
                        tempUnitPrice,
                        tempVat,
                        selectedProduct!!.unitOfMeasure
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
                    userDetail?.salesPersonCode.toString(),
                    "",
                    binding.tvSubTotal.text.toString().replace("ETB", "").toDouble(),
                    binding.tvVat.text.toString().replace("+ ETB", "").toDouble(),
                    customerDetail!!.customerPriceGroup.toString()
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
                            Utils.isNewOrder = true
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

    private fun dialogSingleSelection() {
        filterList!!.clear()
        filterList!!.addAll(itemList)
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_single_selection, null)
        val selectedGroup: String = selectedItem
        val singleItemSelectionAdapter =
            SingleItemSelectionAdapter(this, filterList!!, selectedGroup)

        val lLayout = LinearLayoutManager(this)
        val rvBinList = alertLayout.findViewById<RecyclerView>(R.id.rvItemList)
        rvBinList.layoutManager = lLayout
        rvBinList.adapter = singleItemSelectionAdapter
        val etBinSearch = alertLayout.findViewById<EditText>(R.id.etItemSearch)
        etBinSearch.hint = getString(R.string.search_item)



        etBinSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterList!!.clear()
                if (s.toString().trim().isEmpty()) {
                    filterList!!.addAll(itemList)
                } else {
                    for (item in itemList) {
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
        tvTitle.text = getString(R.string.choose_item)


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
                selectedItem = singleItemSelectionAdapter.selected
                for (item in productList!!) {
                    if (item.itemName == singleItemSelectionAdapter.selected) {
                        selectedProduct = item
                    }
                }
                // Check if item exists in orderList
                val existingOrder = orderList.find { it.itemNo == selectedProduct?.itemNo }
                customerPriceAPI(existingOrder)

                binding.tvItem.text = singleItemSelectionAdapter.selected
                dialog.dismiss()
            }
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
            )!!.webservices.customerPrice(
                customerDetail!!.customerPriceGroup!!, selectedProduct!!.itemNo
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>, response: Response<JsonObject>
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
                                minQty = response.body()!!.asJsonObject.get("minimumQuantity").asInt
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
                this, getString(R.string.please_check_your_internet_connection), Toast.LENGTH_SHORT
            ).show()
        }
    }

//    private fun getLocationCodeList() {
//        locationCodeList.clear()
//
//        val jsonString = SharedHelper.getKey(this, Constants.API_Location_Code)
//
//        if (jsonString.isNotEmpty()) {
//            val dataArray = JsonParser.parseString(jsonString).asJsonObject.getAsJsonArray("data")
//
//            for (item in dataArray) {
//                val code = item.asJsonObject.get("code")?.asString
//                if (!code.isNullOrEmpty()) {
//                    locationCodeList.add(code)
//                }
//            }
//        }
//    }
//
//    private fun getPaymentCodeList() {
//        paymentCodeList.clear()
//
//        val jsonString = SharedHelper.getKey(this, Constants.API_Payment_Code)
//
//        if (jsonString.isNotEmpty()) {
//            val dataArray = JsonParser.parseString(jsonString).asJsonObject.getAsJsonArray("data")
//
//            for (item in dataArray) {
//                val code = item.asJsonObject.get("code")?.asString
//                if (!code.isNullOrEmpty()) {
//                    paymentCodeList.add(code)
//                }
//            }
//        }
//    }

    private fun getItemList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@NewSalesActivity)
            ApiClient.getRestClient(
                Constants.BASE_URL, ""
            )!!.webservices.vanStock(userDetail?.salesPersonCode!!)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>, response: Response<JsonObject>
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