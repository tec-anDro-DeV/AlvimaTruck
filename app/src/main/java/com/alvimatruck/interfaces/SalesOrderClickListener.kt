package com.alvimatruck.interfaces

import com.alvimatruck.model.responses.OrderDetail

interface SalesOrderClickListener {
    fun onOrderClick(orderDetail: OrderDetail)
}