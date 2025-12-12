package com.alvimatruck.model.responses

data class ItemDetail(
    val no: String,
    val description: String,
    var isAdded: Boolean = false
)