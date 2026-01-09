package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.R
import com.alvimatruck.adapter.VanStockListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityVanStockBinding
import com.alvimatruck.model.responses.UserDetail
import com.alvimatruck.model.responses.VanStockDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VanStockActivity : BaseActivity<ActivityVanStockBinding>() {
    private var vanStockListAdapter: VanStockListAdapter? = null

    var userDetail: UserDetail? = null

    var itemList: ArrayList<VanStockDetail>? = ArrayList()
    var filterList: ArrayList<VanStockDetail>? = ArrayList()


    override fun inflateBinding(): ActivityVanStockBinding {
        return ActivityVanStockBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }
        userDetail =
            Gson().fromJson(SharedHelper.getKey(this, Constants.UserDetail), UserDetail::class.java)
        if (intent != null) {
            if (intent.getBooleanExtra(Constants.IS_HIDE, false)) {
                binding.bottomMenu.visibility = View.GONE
                binding.tvTitle.text = getString(R.string.stock_report)
            }
        }
        getItemList()

        binding.rlBottomHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }


        binding.rlBottomTrip.setOnClickListener {

        }

        binding.rlBottomOpreation.setOnClickListener {
            startActivity(Intent(this@VanStockActivity, OperationsActivity::class.java))
        }


        binding.rvStockList.addItemDecoration(
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
                    filterList!!.addAll(itemList!!)
                } else {
                    for (item in itemList!!) {
                        if (item.itemName.lowercase()
                                .contains(s.toString().lowercase()) || item.itemNo.lowercase()
                                .contains(s.toString().lowercase())
                        ) {
                            filterList!!.add(item)
                        }
                    }
                }
                vanStockListAdapter!!.notifyDataSetChanged()
            }
        })

    }


    private fun getItemList() {
        if (Utils.isOnline(this)) {
            ProgressDialog.start(this@VanStockActivity)
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
                                itemList = response.body()!!.getAsJsonArray("data").map {
                                    Gson().fromJson(it, VanStockDetail::class.java)
                                } as ArrayList<VanStockDetail>
                                filterList = ArrayList(itemList!!)
                                if (filterList!!.isNotEmpty()) {
                                    binding.rvStockList.layoutManager = LinearLayoutManager(
                                        this@VanStockActivity, LinearLayoutManager.VERTICAL, false
                                    )


                                    vanStockListAdapter = VanStockListAdapter(
                                        this@VanStockActivity, filterList!!
                                    )
                                    binding.rvStockList.adapter = vanStockListAdapter

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
                                this@VanStockActivity,
                                Utils.parseErrorMessage(response), // Assuming Utils.parseErrorMessage handles this
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        Toast.makeText(
                            this@VanStockActivity,
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

}