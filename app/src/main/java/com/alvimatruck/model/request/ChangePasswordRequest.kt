package com.alvimatruck.model.request

data class ChangePasswordRequest(
    val vanNo: String,
    val newPassword: String
)