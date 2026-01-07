package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils

data class FullOrderDetail(
    val address: String,
    val city: String?,
    val contactNumber: String?,
    val customerName: String,
    val customerPriceGroup: String,
    val invoiceNo: String?,
    val lineNo: Int?,
    val lines: ArrayList<SingleOrder>,
    val orderDate: String,
    val orderId: String?,
    val dotNetOrderId: String,
    val postalCode: String?,
    val status: String,
    val subtotal: Double,
    val total: Double,
    val vat: Double
) {

    fun id(): String {
        return if (orderId.isNullOrEmpty()) {
            dotNetOrderId
        } else {
            orderId
        }
    }

    fun getFormattedContactNo(): String {
        val number = contactNumber?.trim()
        if (number.isNullOrEmpty()) return "-"

        return if (number.startsWith("0")) {
            "+251 " + number.substring(1)
        } else {
            "+251 $number"
        }
    }

    fun getFullAddress(): String {
        var fullAddress = address
        if (!city.isNullOrEmpty()) {
            fullAddress += ", $city"
        }
        if (!postalCode.isNullOrEmpty()) {
            fullAddress += ", $postalCode"
        }
        return fullAddress
    }

    fun getRequestDate(): String {
        return Utils.getFormatedRequestDate(orderDate)
    }
}