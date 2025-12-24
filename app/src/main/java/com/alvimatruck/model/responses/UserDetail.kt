package com.alvimatruck.model.responses

data class UserDetail(
    val driverFullName: String,
    val driverNo: String,
    val email: Any,
    val id: String,
    val isDefaultPassword: Boolean,
    val phoneNumber: String,
    val plateNo: String,
    val roleId: Int,
    val roleName: String,
    val salesPersonCode: String,
    val token: String,
    val userId: String
)