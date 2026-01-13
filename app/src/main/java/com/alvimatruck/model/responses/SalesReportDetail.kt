package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils

data class SalesReportDetail(
    val customerName: String,
    val customerNo: String,
    val description: String,
    val documentNo: String,
    val lineNo: Int,
    val no: String,
    val postingDate: String,
    val quantity: Int,
    val salespersonCode: String,
    val unitOfMeasureCode: String,
    val totalAmount: Int,
    val unitPrice: Double
) {
    fun getRequestDate(): String {
        return Utils.getFormatedRequestDate(postingDate)
    }
}