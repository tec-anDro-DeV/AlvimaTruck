package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils

data class PaymentDetail(
    val createdAt: String,
    val customerName: String,
    val invoiceNumbers: String,
    val paymentCode: String,
    val paymentId: Int,
    val salesPersonCode: String,
    val status: String,
    val totalAmount: Double
) {
    fun getRequestDate(): String {
        return Utils.getFormatedRequestDate(createdAt)
    }
}