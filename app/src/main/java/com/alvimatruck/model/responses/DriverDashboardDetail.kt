package com.alvimatruck.model.responses

data class DriverDashboardDetail(
    val completedOrders: Int,
    val deliveredOrders: Int,
    val pendingOrders: Int
)