package com.alvimatruck.model.responses

data class TransferReciveDetail(
    val documentNo: String,
    val itemNo: String,
    val lineNo: Int,
    val quantityReceived: Int,
    val quantityShipped: Int,
    val unitOfMeasureCode: String,
    var isSelected: Boolean = false,
    var qtyToReceive: Int
)