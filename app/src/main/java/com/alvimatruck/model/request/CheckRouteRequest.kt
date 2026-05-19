package com.alvimatruck.model.request

data class CheckRouteRequest(
    val latitude: Double,
    val longitude: Double,
    val routeName: String,
    val additionalProp1: String = "",
    val additionalProp2: String = "",
    val additionalProp3: String = ""
)