package com.alvimatruck.model.request

data class StartTripRequest(
    val routeName: String,
    val startKm: Int
)