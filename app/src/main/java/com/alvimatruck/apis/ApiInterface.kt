package com.alvimatruck.apis

import com.alvimatruck.model.request.ChangePasswordRequest
import com.alvimatruck.model.request.LoginRequest
import com.alvimatruck.model.request.OTPRequest
import com.alvimatruck.model.request.OTPVerifyRequest
import com.alvimatruck.model.request.ResetPasswordRequest
import com.alvimatruck.utils.Constants
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface ApiInterface {

    @POST(Constants.API_SignIn)
    fun login(@Body loginRequest: LoginRequest): Call<JsonObject>

    @GET(Constants.API_VanList)
    fun vanList(): Call<JsonObject>

    @POST(Constants.API_ResetPassword)
    fun resetPassword(@Body resetPasswordRequest: ResetPasswordRequest): Call<JsonObject>

    @POST(Constants.API_ResendOTP)
    fun resendOtp(@Body otpRequest: OTPRequest): Call<JsonObject>

    @POST(Constants.API_OTPVerify)
    fun otpVerify(@Body otpVerifyRequest: OTPVerifyRequest): Call<JsonObject>

    @POST(Constants.API_ChangePassword)
    fun changePassword(@Body changePasswordRequest: ChangePasswordRequest): Call<JsonObject>


}