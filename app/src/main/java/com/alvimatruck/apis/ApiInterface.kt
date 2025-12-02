package com.alvimatruck.apis

import com.alvimatruck.model.request.ChangePasswordRequest
import com.alvimatruck.model.request.LoginRequest
import com.alvimatruck.model.request.OTPRequest
import com.alvimatruck.model.request.OTPVerifyRequest
import com.alvimatruck.model.request.ResetPasswordRequest
import com.alvimatruck.utils.Constants
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query


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

    @GET(Constants.API_Price_Group)
    fun priceGroupList(): Call<JsonObject>

    @GET(Constants.API_Location_Code)
    fun locationCodeList(): Call<JsonObject>

    @GET(Constants.API_Payment_Code)
    fun paymentCodeList(): Call<JsonObject>

    @GET(Constants.API_Item_List)
    fun itemList(): Call<JsonArray>

    @GET(Constants.API_Today_Routes)
    fun getTodayRoutes(): Call<JsonObject>

    @GET(Constants.API_Customer_List)
    fun customerList(@Query("page") page: Int, @Query("pageSize") pageSize: Int): Call<JsonObject>

    @Multipart
    @POST(Constants.API_Create_Customer)
    fun createCustomer(
        @Part("CustomerName") customerName: RequestBody,
        @Part("ContactName") contactName: RequestBody,
        @Part("CustomerPhoneNumber") customerPhoneNumber: RequestBody,
        @Part("TeleNumber") teleNumber: RequestBody,
        @Part("City") city: RequestBody,
        @Part("PostalCode") postalCode: RequestBody,
        @Part("TinNumber") tinNumber: RequestBody,
        @Part("Address") address: RequestBody,
        @Part("CustomerPostingGroup") customerPostingGroup: RequestBody,
        @Part("CustomerPriceGroup") customerPriceGroup: RequestBody,
        @Part("Latitude") latitude: RequestBody,
        @Part("Longitude") longitude: RequestBody,
        @Part customerImage: MultipartBody.Part? = null,   // optional
        @Part idProof: MultipartBody.Part? = null          // optional
    ): Call<JsonObject>
}