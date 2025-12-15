package com.alvimatruck.interfaces

import com.alvimatruck.model.responses.SingleOrder

interface DeleteOrderListener {
    fun onDeleteOrder(orderDetail: SingleOrder)
}