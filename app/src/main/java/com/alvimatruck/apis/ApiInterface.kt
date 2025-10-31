package com.alvimatruck.apis

import com.alvimatruck.model.request.LoginRequest
import com.alvimatruck.utils.Constants
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiInterface {

    @POST(Constants.API_SignIn)
    fun login(@Body loginRequest: LoginRequest): Call<JsonObject>


}