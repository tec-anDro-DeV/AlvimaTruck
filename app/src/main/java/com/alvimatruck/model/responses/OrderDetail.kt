package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils.to2Decimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

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
        return LocalDateTime.parse(
            orderDate, DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                .optionalEnd()
                .toFormatter()
        ).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}