package com.alvimatruck.model.request

import com.google.gson.annotations.SerializedName

data class LoginRequest(

    @SerializedName("password") val password: String? = null,

    @SerializedName("userID") val userID: String? = null
)
