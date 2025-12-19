package com.alvimatruck.model.responses

data class VanStockDetail(
    val entryNo: Int,
    val itemName: String,
    val itemNo: String,
    val qtyOnHand: Int,
    val salespersonCode: String,
    val unitOfMeasure: String
)