package com.alvimatruck.model.request

data class EndTripRequest(
    val routeName: String,
    val endKm: Int
)