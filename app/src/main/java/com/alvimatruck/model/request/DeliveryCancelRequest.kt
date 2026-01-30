package com.alvimatruck.model.request

data class DeliveryCancelRequest(
    val bcOrderNo: String,
    val reason: String,
    val endKm: Int,
    val customerGeoLocation: String
)