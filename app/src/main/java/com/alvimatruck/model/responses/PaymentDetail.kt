package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils
import com.alvimatruck.utils.Utils.to2Decimal

data class PaymentDetail(
    val postingDate: String,
    val customerName: String,
    val invoiceNumbers: String,
    val paymentCode: String,
    val paymentId: Int,
    val salesPersonCode: String,
    val status: String,
    val totalAmount: Double,
    val amount: Double
) {
    fun getRequestDate(): String {
        return Utils.getFormatedRequestDate(postingDate)
    }

    fun formatedAmount(): String {
        return amount.to2Decimal()
    }
}