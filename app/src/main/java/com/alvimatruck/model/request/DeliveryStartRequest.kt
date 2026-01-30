package com.alvimatruck.model.request

data class DeliveryStartRequest(
    val bcOrderNo: String,
    val driverNo: String,
    val startKM: Int
)