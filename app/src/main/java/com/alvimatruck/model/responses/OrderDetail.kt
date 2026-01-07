package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils
import com.alvimatruck.utils.Utils.to2Decimal

data class OrderDetail(
    val customerName: String,
    val orderDate: String,
    val orderId: String?,
    val dotNetOrderId: String,
    val routeName: String,
    val status: String,
    val subTotal: Double,
    val invoiceNo: String?
) {
    fun id(): String {
        return if (orderId.isNullOrEmpty()) {
            dotNetOrderId
        } else {
            orderId
        }
    }

    fun subTotal(): String {
        return "ETB " + subTotal.to2Decimal()
    }

    fun getRequestDate(): String {
        return Utils.getFormatedRequestDate(orderDate)
    }
}