package com.alvimatruck.model.responses

data class VanStockDetail(
    val entryNo: Int,
    val itemName: String,
    val itemNo: String,
    var qtyOnHand: Int,
    val salespersonCode: String,
    val unitOfMeasure: String
)