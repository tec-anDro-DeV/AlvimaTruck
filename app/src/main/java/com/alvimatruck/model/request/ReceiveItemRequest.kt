package com.alvimatruck.model.request

data class ReceiveItemRequest(
    val lineNo: Int,
    val qtyToReceive: Int,
    val transferOrderNo: String
)