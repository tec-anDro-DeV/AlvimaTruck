package com.alvimatruck.model.responses

data class DashboardDetails(
    val activeRoute: ActiveRoute?,
    val routesCompletedCount: Int,
    val todayCollectionsCount: Int,
    val todaySalesCount: Int
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
    val visited: Int
)