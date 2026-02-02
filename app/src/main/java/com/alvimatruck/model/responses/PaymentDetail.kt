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
    val amount: Double,
    val markPaymentDate: String
) {
    fun getRequestDate(): String {
        return if (status == "Paid") {
            Utils.getFormatedRequestDate(postingDate)
        } else {
            Utils.getFormatedRequestDate(markPaymentDate)
        }
    }

    fun formatedAmount(): String {
        return amount.to2Decimal()
    }
}