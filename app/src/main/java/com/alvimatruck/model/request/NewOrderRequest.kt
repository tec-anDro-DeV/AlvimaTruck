package com.alvimatruck.model.request

import com.alvimatruck.model.responses.SingleOrder

data class NewOrderRequest(
    val customerName: String,
    val customerNo: String,
    val finalTotal: Double,
    val lines: ArrayList<SingleOrder>,
    val locationCode: String,
    val paymentCode: String,
    val subTotal: Double,
    val totalVat: Double,
    val customerPriceGroup: String
)