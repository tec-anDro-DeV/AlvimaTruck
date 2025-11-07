package com.alvimatruck.model.responses

data class UserDetail(
    val active: Int,
    val currentLocation: String,
    val driverNo: String,
    val email: String,
    val firstName: String,
    val id: Int,
    val isDefaultPassword: Boolean,
    val isDelivery: Int,
    val lastName: String,
    val phoneNumber: String,
    val plateNo: String,
    val profileImagePath: String,
    val roleId: Int,
    val roleName: String,
    val supervisor: String,
    val token: String,
    val userAlias: String
)