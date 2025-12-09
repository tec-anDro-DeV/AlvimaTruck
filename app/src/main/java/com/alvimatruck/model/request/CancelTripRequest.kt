package com.alvimatruck.model.request

data class CancelTripRequest(
    val routeName: String,
    val cancelReason: String
)