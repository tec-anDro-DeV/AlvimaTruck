package com.alvimatruck.model.request

data class ResetPasswordRequest(
    val oldPassword: String,
    val newPassword: String,
)