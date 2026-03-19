package com.alvimatruck.model.request

data class StartDayRequest(
    val driverNo: String,
    val startKM: Int,
)

data class EndDayRequest(
    val driverNo: String,
    val endKM: Int,
)