package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils

data class RequisitionDetail(
    val fromLocation: String,
    val id: Int,
    val no: String,
    val requestDate: String,
    val status: String,
    val toLocation: String,
    var isSelected: Boolean = false
) {
    fun getRequestedDate(): String {
        return Utils.getFormatedRequestDate(requestDate)
    }
}