package com.alvimatruck.model.responses

data class RouteDetail(
    val locations: ArrayList<Location>,
    val regularCustomerCount: Int,
    val visited: Int,
    val skipped: Int,
    val distance: Double,
    val startKm: Double,
    val endKm: Double,
    val routeName: String,
    val totalSalesValues: Double,
    val territory: String,
    val status: String
)