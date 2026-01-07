package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils

data class TransferDetail(
    val postingDate: String,
    val transferFromCode: String,
    val transferOrderNo: String,
    val transferToCode: String,
    var isSelected: Boolean = false
) {
    fun getRequestDate(): String {
        return Utils.getFormatedRequestDate(postingDate)
    }
}