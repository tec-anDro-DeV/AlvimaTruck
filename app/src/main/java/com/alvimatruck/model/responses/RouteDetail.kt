package com.alvimatruck.model.responses

data class RouteDetail(
    val locations: ArrayList<Location>,
    val regularCustomerCount: Int,
    val visited: Int,
    val skipped: Int,
    val routeName: String,
    val territory: String
)