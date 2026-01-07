package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils

data class InvoiceDetail(
    val documentNo: String, val postingDate: String, val remainingAmount: Double
) {
    fun getRequestDate(): String {
        return Utils.getFormatedRequestDate(postingDate)
    }
}