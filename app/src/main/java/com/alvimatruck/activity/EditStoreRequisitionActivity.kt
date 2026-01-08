package com.alvimatruck.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvimatruck.R
import com.alvimatruck.adapter.SingleItemSelectionAdapter
import com.alvimatruck.adapter.TransferRequestItemListAdapter
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityEditStoreRequisitionBinding
import com.alvimatruck.interfaces.DeleteTransferRequestListener
import com.alvimatruck.model.responses.ItemDetail
import com.alvimatruck.model.responses.SingleTransfer
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.SharedHelper
import com.google.gson.Gson
import com.google.gson.JsonParser

class EditStoreRequisitionActivity : BaseActivity<ActivityEditStoreRequisitionBinding>(),
    DeleteTransferRequestListener {
    var isChange: Boolean = false

    var itemList: ArrayList<String> = ArrayList()

    var filterList: ArrayList<String>? = ArrayList()

    var selectedItem = ""


    var selectedProduct: ItemDetail? = null


    var userDetail: UserDetail? = null

    var storeRequisitionRequestItemListAdapter: TransferRequestItemListAdapter? = null

    var requestList: ArrayList<SingleTransfer> = ArrayList()

    var productList: ArrayList<ItemDetail>? = ArrayList()


    override fun inflateBinding(): ActivityEditStoreRequisitionBinding {
        return ActivityEditStoreRequisitionBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userDetail =
            Gson().fromJson(SharedHelper.getKey(this, Constants.UserDetail), UserDetail::class.java)


        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        getItemList()

        binding.tvItem.setOnClickListener {
            dialogSingleSelection(
                itemList,
                getString(R.string.choose_item),
                getString(R.string.search_item),
                binding.tvItem
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
                binding.tvUpdateRequest.visibility = View.VISIBLE
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
            this@EditStoreRequisitionActivity, requestList, this@EditStoreRequisitionActivity
        )
        binding.rvTransferList.adapter = storeRequisitionRequestItemListAdapter

        binding.tvUpdateRequest.setOnClickListener {
            if (requestList.isEmpty()) {
                Toast.makeText(
                    this, getString(R.string.please_add_items_to_order), Toast.LENGTH_SHORT
                ).show()
            } else {
                //  updateRequestAPI()
            }
        }



        binding.tvDelete.setOnClickListener {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.dialog_alert_two_button, null)

            val tvTitle = alertLayout.findViewById<TextView>(R.id.tvTitle)
            val tvMessage = alertLayout.findViewById<TextView>(R.id.tvMessage)
            val btnNo = alertLayout.findViewById<TextView>(R.id.btnNo)
            val btnYes = alertLayout.findViewById<TextView>(R.id.btnYes)

            // Set content
            tvTitle.text = getString(R.string.delete_request)
            tvMessage.text = getString(R.string.are_you_sure_you_want_to_delete_this_request)
            btnNo.text = getString(R.string.no)
            btnYes.text = getString(R.string.yes)


            val dialog =
                AlertDialog.Builder(this).setView(alertLayout).setCancelable(false).create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)


            btnNo.setOnClickListener {
                dialog.dismiss()
            }
            btnYes.setOnClickListener {
                dialog.dismiss()
                // deleteOrderAPI()
            }

            dialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% of screen width
            dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)


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

    private fun dialogSingleSelection(
        list: ArrayList<String>, title: String, hint: String, textView: TextView
    ) {
        filterList!!.clear()
        filterList!!.addAll(list)
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
                textView.text = singleItemSelectionAdapter.selected
                dialog.dismiss()
            }
        }
    }


    override fun onDeleteRequest(request: SingleTransfer) {
        requestList.remove(request)
        storeRequisitionRequestItemListAdapter!!.notifyDataSetChanged()
        if (requestList.isEmpty()) {
            binding.llRequestList.visibility = View.GONE
            binding.tvUpdateRequest.visibility = View.GONE
        }
    }

    override fun handleBackPressed(callback: OnBackPressedCallback?) {
        if (isChange) {
            setResult(RESULT_OK)
        }
        finish()
        super.handleBackPressed(callback)
    }
}