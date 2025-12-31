package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils.to2Decimal

data class SingleOrder(
    var finalPrice: Double,
    var itemName: String,
    var itemNo: String,
    var quantity: Int,
    var unitPrice: Double,
    var vat: Double,
    var unitOfMeasure: String,
    var lineNo: Int = 0,
    var isDelete: Boolean = false
) {
    fun finalPrice(): String {
        return finalPrice.to2Decimal()
    }

    fun singleUnitPriceWithTax(): String {
        return (unitPrice + vat).to2Decimal()
    }
}