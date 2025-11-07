package com.alvimatruck.model.request

data class LoginRequest(
    val password: String,
    val salesPerson: String,
    val vanNo: String
)