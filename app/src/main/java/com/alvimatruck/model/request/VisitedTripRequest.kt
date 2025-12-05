package com.alvimatruck.model.request

data class VisitedTripRequest(
    val customerId: String,
    val reason: String
)