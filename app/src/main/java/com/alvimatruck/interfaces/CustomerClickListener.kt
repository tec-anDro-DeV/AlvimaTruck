package com.alvimatruck.interfaces

import com.alvimatruck.model.responses.CustomerDetail

interface CustomerClickListener {
    fun onCustomerClick(customerDetail: CustomerDetail)
}