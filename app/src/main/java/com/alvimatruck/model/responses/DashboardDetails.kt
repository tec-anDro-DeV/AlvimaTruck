package com.alvimatruck.model.responses

data class DashboardDetails(
    val activeRoute: ActiveRoute?,
    val totalCustomerVisits: Int,
    val todayCreditSales: Double,
    val todayCashSales: Double,
    val todayCashCollections: Double,
    val todayCreditCollections: Double
)

data class ActiveRoute(
    val distance: Double,
    val endKm: Int,
    val isSuccess: Boolean,
    val locations: ArrayList<Location>,
    val regularCustomerCount: Int,
    val routeName: String,
    val skipped: Int,
    val startKm: Double,
    val status: String,
    val totalSalesValues: Int,
    val soldCustomers: Int,
    val visitedCustomers: Int
)