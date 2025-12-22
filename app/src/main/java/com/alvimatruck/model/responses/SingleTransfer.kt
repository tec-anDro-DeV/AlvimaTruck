package com.alvimatruck.model.responses

data class SingleTransfer(
    var itemName: String,
    var itemNo: String,
    var quantity: Int,
    var unitOfMeasureCode: String,
)