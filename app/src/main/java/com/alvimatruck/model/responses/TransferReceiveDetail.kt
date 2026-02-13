package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils

data class TransferReceiveDetail(
    val documentNo: String,
    val itemNo: String,
    val lineNo: Int,
    val quantityReceived: Int,
    val quantityShipped: Int,
    val unitOfMeasureCode: String,
    val description: String,
    var isSelected: Boolean = false,
    var qtyToReceive: Int,
    var postingDate: String
) {
    fun getFormatedDate(): String {
        return Utils.getFormatedRequestDate(postingDate)
    }
}