package com.alvimatruck.model.request

data class CustomerUpdate(
    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val no: String,
    val postalCode: String,
    val teleNumber: String
)