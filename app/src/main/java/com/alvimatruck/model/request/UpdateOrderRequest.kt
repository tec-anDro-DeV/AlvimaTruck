package com.alvimatruck.model.request

import com.alvimatruck.model.responses.SingleOrder

data class UpdateOrderRequest(
    val finalTotal: Double,
    val lines: ArrayList<SingleOrder>,
    val subTotal: Double,
    val totalVat: Double,
    val documentNo: String
)