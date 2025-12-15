package com.alvimatruck.model.responses

data class SingleOrder(
    var finalPrice: Double,
    var itemName: String,
    var itemNo: String,
    var quantity: Int,
    var unitPrice: Double,
    var vat: Double,
    var unitOfMeasure: String
)