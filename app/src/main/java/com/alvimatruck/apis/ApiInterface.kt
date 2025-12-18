package com.alvimatruck.apis

import com.alvimatruck.model.request.CancelTripRequest
import com.alvimatruck.model.request.ChangePasswordRequest
import com.alvimatruck.model.request.CustomerUpdate
import com.alvimatruck.model.request.EndTripRequest
import com.alvimatruck.model.request.LoginRequest
import com.alvimatruck.model.request.NewOrderRequest
import com.alvimatruck.model.request.OTPRequest
import com.alvimatruck.model.request.OTPVerifyRequest
import com.alvimatruck.model.request.ResetPasswordRequest
import com.alvimatruck.model.request.StartTripRequest
import com.alvimatruck.model.request.VisitedTripRequest
import com.alvimatruck.utils.Constants
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url


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

    @GET(Constants.API_Today_Routes)
    fun getTodayRoutes(): Call<JsonObject>

    @GET(Constants.API_Sales_Orders)
    fun getSalesOrder(): Call<JsonObject>

    @POST(Constants.API_Start_Trip)
    fun startTrip(@Body startTripRequest: StartTripRequest): Call<JsonObject>

    @POST(Constants.API_End_Trip)
    fun endTrip(@Body endTripRequest: EndTripRequest): Call<JsonObject>

    @POST(Constants.API_Visit_Trip)
    fun visitTrip(@Body visitedTripRequest: VisitedTripRequest): Call<JsonObject>

    @POST(Constants.API_Cancel_Trip)
    fun cancelTrip(@Body cancelTripRequest: CancelTripRequest): Call<JsonObject>

    @POST(Constants.API_New_Order)
    fun newOrder(@Body newOrderRequest: NewOrderRequest): Call<JsonObject>

    @GET(Constants.API_Customer_List)
    fun customerList(
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("routeName") routeName: String? = null
    ): Call<JsonObject>

    @GET(Constants.API_Order_Detail + "/{orderId}")
    fun orderDetail(
        @Path("orderId") orderId: String
    ): Call<JsonObject>

    @GET(Constants.API_Fleet_Log_List)
    fun fleetLogList(
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
    ): Call<JsonObject>

    @GET(Constants.API_Customer_Price)
    fun customerPrice(
        @Query("salesCode") salesCode: String? = null,
        @Query("itemNo") itemNo: String? = null,
    ): Call<JsonObject>

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

    @PUT(Constants.API_Update_Customer)
    fun updateCustomer(@Body updateCustomerRequest: CustomerUpdate): Call<JsonObject>

    @Multipart
    @POST(Constants.API_Fleet)
    fun fuelRequest(
        @Part("FleetType") FleetType: RequestBody,
        @Part("FuelRefillAmount") fuelRefillAmount: RequestBody,
        @Part("Latitude") latitude: RequestBody,
        @Part("Longitude") longitude: RequestBody,
        @Part fuelRefillMeter: MultipartBody.Part? = null
    ): Call<JsonObject>

    @Multipart
    @POST(Constants.API_Fleet)
    fun repairLogRequest(
        @Part("FleetType") FleetType: RequestBody,
        @Part("Latitude") latitude: RequestBody,
        @Part("Longitude") longitude: RequestBody,
        @Part("RepairLogVendorDetail") vendorDetail: RequestBody,
        @Part("RepairLogRepairCost") repairCost: RequestBody,
        @Part repairLogReplacePart: List<MultipartBody.Part> // <-- multiple files
    ): Call<JsonObject>


    @Multipart
    @POST(Constants.API_Fleet)
    fun incidentReportRequest(
        @Part("FleetType") FleetType: RequestBody,
        @Part("Latitude") latitude: RequestBody,
        @Part("Longitude") longitude: RequestBody,
        @Part("IncidentReportType") incidentReportType: RequestBody,
        @Part("IncidentReportDescription") incidentReportDescription: RequestBody,
        @Part repairLogReplacePart: List<MultipartBody.Part> // <-- multiple files
    ): Call<JsonObject>

    @GET
    suspend fun getDropdownData(@Url url: String): Response<JsonElement>


}