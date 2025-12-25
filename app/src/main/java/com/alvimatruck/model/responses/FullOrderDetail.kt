package com.alvimatruck.model.responses

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

data class FullOrderDetail(
    val address: String,
    val city: String?,
    val contactNumber: String?,
    val customerName: String,
    val customerPriceGroup: String,
    val invoiceNo: String,
    val lines: ArrayList<SingleOrder>,
    val orderDate: String,
    val orderId: String,
    val postalCode: String?,
    val status: String,
    val subtotal: Double,
    val total: Double,
    val vat: Double
) {
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