package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils
import com.alvimatruck.utils.Utils.to2Decimal

data class CreditPaymentDetail(
    val postingDate: String,
    val customerName: String,
    val paymentCode: String,
    val documentNo: String,
    val salesPersonCode: String,
    val totalAmount: Double,
    val amount: Double,
    val markPaymentDate: String,
    val paymentInProcess: Boolean,
    val paymentStatus: String
) {
    fun getRequestDate(): String {
        return Utils.getFormatedRequestDate(markPaymentDate)
//        return if (!paymentInProcess) {
//            Utils.getFormatedRequestDate(postingDate)
//        } else {
//            Utils.getFormatedRequestDate(markPaymentDate)
//        }
    }

    fun formatedAmount(): String {
        return amount.to2Decimal()
    }
}