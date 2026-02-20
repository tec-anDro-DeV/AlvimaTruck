package com.alvimatruck.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvimatruck.adapter.CustomerListAdapter
import com.alvimatruck.apis.ApiClient
import com.alvimatruck.custom.BaseActivity
import com.alvimatruck.custom.EqualSpacingItemDecoration
import com.alvimatruck.databinding.ActivityCustomersBinding
import com.alvimatruck.interfaces.CustomerClickListener
import com.alvimatruck.model.responses.CustomerDetail
import com.alvimatruck.utils.Constants
import com.alvimatruck.utils.ProgressDialog
import com.alvimatruck.utils.SharedHelper
import com.alvimatruck.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomersActivity : BaseActivity<ActivityCustomersBinding>(), CustomerClickListener {
    private var customerListAdapter: CustomerListAdapter? = null

    var routeName = ""
    var customerList: ArrayList<CustomerDetail>? = ArrayList()
    var filterList: ArrayList<CustomerDetail>? = ArrayList()

    var page: Int = 1
    var pageSize: Int = 20

    private var isLoading = false
    private var isLastPage = false

    // For search debouncing (prevents API call on every single keystroke)
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null


    private val openUpdateCustomer =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedCustomer = Gson().fromJson(
                    result.data?.getStringExtra(Constants.CustomerDetail).toString(),
                    CustomerDetail::class.java
                )

                if (updatedCustomer != null) {
                    val index =
                        filterList!!.indexOfFirst { it.no == updatedCustomer.no }  // match customerId
                    if (index != -1) {
                        filterList!![index] = updatedCustomer
                        customerListAdapter!!.notifyItemChanged(index)
                    }
                }
            }
        }


    override fun inflateBinding(): ActivityCustomersBinding {
        return ActivityCustomersBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnBack.setOnClickListener {
            handleBackPressed()
        }

        if (intent != null) {
            routeName = intent.getStringExtra(Constants.RouteDetail).toString()
        }


        binding.rvCustomerList.addItemDecoration(
            EqualSpacingItemDecoration(
                resources.getDimension(com.intuit.sdp.R.dimen._12sdp).toInt(),
                EqualSpacingItemDecoration.VERTICAL
            )
        )

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvCustomerList.layoutManager = layoutManager


        // Scroll Listener for Pagination
        binding.rvCustomerList.addOnScrollListener(object :
            androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Check if not loading, not the last page, and reached the bottom
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= pageSize
                    ) {
                        page++
                        customerListAPI()
                    }
                }
            }
        })

        customerListAPI()


        binding.ivAddCustomer.setOnClickListener {
            // startActivity(Intent(this@CustomersActivity, CreateCustomerActivity::class.java))
            val intent = Intent(this, CreateCustomerActivity::class.java)
            startForResult.launch(intent)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Debounce search to save API calls
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    val searchQuery = s.toString().trim()
                    if (searchQuery.isEmpty() || searchQuery.length >= 3) {
                        page = 1
                        isLastPage = false
                        // We don't clear the list here; the API call with page=1 will replace it
                        customerListAPI()
                    }
                }
                searchHandler.postDelayed(searchRunnable!!, 500) // Wait 500ms after typing stops
            }
        })
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // NEW CUSTOMER ADDED → Refresh list
            page = 1
            isLastPage = false // Important: allow pagination to start over

            // Optional: clear existing lists to show a clean loading state
            customerList?.clear()
            filterList?.clear()
            binding.etSearch.setText("")
            customerListAPI()
        }
    }

    private fun customerListAPI() {
        if (Utils.isOnline(this)) {
            isLoading = true
            ProgressDialog.start(this@CustomersActivity)

            ApiClient.getRestClient(
                Constants.BASE_URL, SharedHelper.getKey(this, Constants.Token)
            )!!.webservices.customerList(
                page = page,
                pageSize = pageSize,
                routeName = routeName,
                binding.etSearch.text.toString().trim().ifEmpty { null }
            )
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        isLoading = false
                        ProgressDialog.dismiss()

                        if (response.code() == 401) {
                            Utils.forceLogout(this@CustomersActivity)
                            return
                        }

                        if (response.isSuccessful) {
                            try {
                                val itemsArray = response.body()!!.getAsJsonArray("items")
                                val newItems = itemsArray.map {
                                    Gson().fromJson(it, CustomerDetail::class.java)
                                } as ArrayList<CustomerDetail>

                                // If fewer items returned than page size, it's the last page
                                if (newItems.size < pageSize) {
                                    isLastPage = true
                                }

                                if (page == 1) {
                                    // First load or refresh
                                    customerList = newItems
                                    filterList = ArrayList(customerList!!)
                                    customerListAdapter = CustomerListAdapter(
                                        this@CustomersActivity, filterList!!, this@CustomersActivity
                                    )
                                    binding.rvCustomerList.adapter = customerListAdapter
                                } else {
                                    // Pagination load: Append data
                                    val startPosition = filterList!!.size
                                    customerList!!.addAll(newItems)
                                    filterList!!.addAll(newItems)
                                    customerListAdapter!!.notifyItemRangeInserted(
                                        startPosition,
                                        newItems.size
                                    )
                                }

                                if (filterList!!.isNotEmpty()) {
                                    binding.rvCustomerList.visibility = View.VISIBLE
                                    binding.llNoData.root.visibility = View.GONE
                                } else if (page == 1) {
                                    binding.rvCustomerList.visibility = View.GONE
                                    binding.llNoData.root.visibility = View.VISIBLE
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        isLoading = false
                        ProgressDialog.dismiss()
                        Toast.makeText(
                            this@CustomersActivity,
                            getString(com.alvimatruck.R.string.api_fail_message),
                            Toast.LENGTH_SHORT
                        ).show()
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

    override fun onCustomerClick(customerDetail: CustomerDetail) {
        val intent = Intent(this, ViewCustomerActivity::class.java).putExtra(
            Constants.CustomerDetail, Gson().toJson(customerDetail)
        )
        openUpdateCustomer.launch(intent)
    }

    override fun handleBackPressed(callback: OnBackPressedCallback?) {
        if (Utils.isNewOrder) {
            val intent = Intent(this, TripRouteListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        } else {
            super.handleBackPressed(callback)
        }
    }


}