package com.alvimatruck.model.responses

data class ItemDetail(
    val no: String,
    val description: String,
    val baseUnitOfMeasure: String,
    var isAdded: Boolean = false
)