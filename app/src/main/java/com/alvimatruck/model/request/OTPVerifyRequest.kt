package com.alvimatruck.model.request

data class OTPVerifyRequest(
    val vanNo: String,
    val otp: String
)