package com.alvimatruck.model.responses

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

data class InvoiceDetail(
    val documentNo: String,
    val postingDate: String,
    val remainingAmount: Double
) {
    fun getRequestDate(): String {
        return LocalDateTime.parse(
            postingDate, DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                .optionalEnd()
                .toFormatter()
        ).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}