package com.alvimatruck.model.request

data class CheckRouteRequest(
    val latitude: Double,
    val longitude: Double,
    val routeName: String
)