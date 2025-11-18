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
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.databinding.ActivityCreateCustomerBinding

class CreateCustomerActivity : BaseActivity<ActivityCreateCustomerBinding>() {
    var itemList: ArrayList<String>? = ArrayList()
    var filterList: ArrayList<String>? = ArrayList()
    var selectedGroup: String? = null


    override fun inflateBinding(): ActivityCreateCustomerBinding {
        return ActivityCreateCustomerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        itemList!!.add("Group 1")
        itemList!!.add("Group 2")
        itemList!!.add("Group 3")
        itemList!!.add("Group 4")
        itemList!!.add("Group 5")
        itemList!!.add("Group 6")

        binding.tvCustomerPriceGroup.setOnClickListener {
            dialogSingleSelection(
                itemList!!,
                "Choose Price Group",
                "Search Price Group",
                binding.tvCustomerPriceGroup
            )
        }

        binding.tvCustomerPostingGroup.setOnClickListener {
            dialogSingleSelection(
                itemList!!,
                "Choose Posting Group",
                "Search Posting Group",
                binding.tvCustomerPostingGroup
            )
        }

        binding.tvCancel.setOnClickListener {
            handleBackPressed()
        }

        binding.tvCreate.setOnClickListener {
            handleBackPressed()
        }

    }

    private fun dialogSingleSelection(
        list: ArrayList<String>,
        title: String,
        hint: String,
        textView: TextView
    ) {
        filterList!!.addAll(list)
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_single_selection, null)

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
            selectedGroup = singleItemSelectionAdapter.selected!!
            textView.text = singleItemSelectionAdapter.selected!!
            dialog.dismiss()
        }
    }

}