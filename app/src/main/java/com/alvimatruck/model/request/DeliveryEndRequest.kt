package com.alvimatruck.model.request

data class DeliveryEndRequest(
    val bcOrderNo: String,
    val endKM: Int
)