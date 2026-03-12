package com.alvimatruck.model.responses

data class RouteDetail(
    val locations: ArrayList<Location>,
    val regularCustomerCount: Int,
    val skipped: Int,
    val distance: Double,
    val startKm: Int,
    val endKm: Int,
    val routeName: String,
    val totalSalesValues: Double,
    val territory: String,
    val status: String,
    val soldCustomers: Int,
    val visitedCustomers: Int
)