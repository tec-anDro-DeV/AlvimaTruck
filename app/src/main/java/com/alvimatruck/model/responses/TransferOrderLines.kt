package com.alvimatruck.model.responses

data class TransferOrderLines(
    val description: String,
    val itemNo: String,
    val postingDate: String,
    val quantity: Int,
    val unitOfMeasureCode: String
)