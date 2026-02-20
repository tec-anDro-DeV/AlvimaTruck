package com.alvimatruck.apis

import com.alvimatruck.model.request.CancelTripRequest
import com.alvimatruck.model.request.ChangePasswordRequest
import com.alvimatruck.model.request.CustomerUpdate
import com.alvimatruck.model.request.DeliveryCancelRequest
import com.alvimatruck.model.request.DeliveryEndRequest
import com.alvimatruck.model.request.DeliveryStartRequest
import com.alvimatruck.model.request.EndTripRequest
import com.alvimatruck.model.request.LoginRequest
import com.alvimatruck.model.request.NewOrderRequest
import com.alvimatruck.model.request.OTPRequest
import com.alvimatruck.model.request.OTPVerifyRequest
import com.alvimatruck.model.request.OrderPostRequest
import com.alvimatruck.model.request.ReceiveItemRequest
import com.alvimatruck.model.request.ResetPasswordRequest
import com.alvimatruck.model.request.StartTripRequest
import com.alvimatruck.model.request.StoreRequisitionApproveRequest
import com.alvimatruck.model.request.StoreRequisitionRequest
import com.alvimatruck.model.request.TransferPostRequest
import com.alvimatruck.model.request.TransferRequest
import com.alvimatruck.model.request.UpdateOrderRequest
import com.alvimatruck.model.request.VisitedTripRequest
import com.alvimatruck.utils.Constants
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
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

    @GET(Constants.API_Payment_Logs)
    fun getPaymentLogs(): Call<JsonObject>

    @GET(Constants.API_Sales_Orders)
    fun getSalesOrder(): Call<JsonObject>

    @GET(Constants.API_Dashboard_Report)
    fun getSalesDashboardReport(): Call<JsonObject>

    @GET(Constants.API_Store_Requisition_List)
    fun getStoreRequisitionList(): Call<JsonObject>

    @POST(Constants.API_Start_Trip)
    fun startTrip(@Body startTripRequest: StartTripRequest): Call<JsonObject>

    @POST(Constants.API_Start_DriverTrip)
    fun startDriverTrip(@Body deliveryStartRequest: DeliveryStartRequest): Call<JsonObject>

    @POST(Constants.API_End_DriverTrip)
    fun endDriverTrip(@Body deliveryEndRequest: DeliveryEndRequest): Call<JsonObject>

    @POST(Constants.API_End_Trip)
    fun endTrip(@Body endTripRequest: EndTripRequest): Call<JsonObject>

    @POST(Constants.API_Visit_Trip)
    fun visitTrip(@Body visitedTripRequest: VisitedTripRequest): Call<JsonObject>

    @POST(Constants.API_Cancel_Trip)
    fun cancelTrip(@Body cancelTripRequest: CancelTripRequest): Call<JsonObject>

    @POST(Constants.API_Cancel_DriverTrip)
    fun cancelDriverTrip(@Body deliveryCancelRequest: DeliveryCancelRequest): Call<JsonObject>

    @POST(Constants.API_Transfer_Receive)
    fun receiveItem(@Body receiveItemRequest: ReceiveItemRequest): Call<JsonObject>

    @POST(Constants.API_New_Order)
    fun newOrder(@Body newOrderRequest: NewOrderRequest): Call<JsonObject>

    @PATCH(Constants.API_Update_Order)
    fun updateOrder(@Body updateOrderRequest: UpdateOrderRequest): Call<JsonObject>

    @POST(Constants.API_Create_Transfer)
    fun newTransferRequest(@Body transferRequest: TransferRequest): Call<JsonObject>

    @POST(Constants.API_Transfer_Post)
    fun transferPost(@Body transferPostRequest: TransferPostRequest): Call<JsonObject>

    @POST(Constants.API_Create_Store_Requisition)
    fun newRequisitionRequest(@Body storeRequisitionRequest: StoreRequisitionRequest): Call<JsonObject>

    @POST(Constants.API_Store_Requisition_Approve)
    fun storeRequisitionApproveRequest(@Body storeRequisitionApproveRequest: StoreRequisitionApproveRequest): Call<JsonObject>

    @GET(Constants.API_Customer_List)
    fun customerList(
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("routeName") routeName: String? = null,
        @Query("search") search: String? = null
    ): Call<JsonObject>

    @GET(Constants.API_Sales_Report)
    fun salesReportList(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): Call<JsonObject>

    @GET(Constants.API_Trip_Report)
    fun tripReport(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): Call<JsonObject>

    @GET(Constants.API_Driver_Trip_Report)
    fun driverTripReport(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): Call<JsonObject>

    @GET(Constants.API_Transfer_List)
    fun transferList(
        @Query("transferFromCode") transferFromCode: String,
    ): Call<JsonObject>

    @GET(Constants.API_Transfer_Lines)
    fun transferLines(): Call<JsonObject>

    @GET(Constants.API_Check_Route)
    fun routeCheck(
        @Query("route") route: String,
    ): Call<JsonObject>

    @GET(Constants.API_Invoice_List)
    fun invoiceList(
        @Query("customerNo") customerNo: String,
    ): Call<JsonObject>

    @DELETE(Constants.API_Delete_Order + "{orderId}")
    fun deleteOrder(
        @Path("orderId") orderId: String
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

    @GET(Constants.API_Driver_Trip)
    fun driverTripList(
        @Query("DriverNo") DriverNo: String? = null,
        @Query("ShipmentDate") ShipmentDate: String? = null,
    ): Call<JsonObject>

    @GET(Constants.API_Customer_Price)
    fun customerPrice(
        @Query("salesCode") salesCode: String,
        @Query("itemNo") itemNo: String,
    ): Call<JsonObject>

    @GET(Constants.API_Van_Stock)
    fun vanStock(
        @Query("salespersonCode") salespersonCode: String,
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

    @Multipart
    @POST(Constants.API_Payment_Create)
    fun paymentCreate(
        @Part("CustNo") CustNo: RequestBody,
        //  @Part("CustomerName") CustomerName: RequestBody,
//        @Part("PaymentCode") PaymentCode: RequestBody,
        //  @Part("TotalAmount") TotalAmount: RequestBody,
        @Part("BankReferenceNo") BankReferenceNo: RequestBody,
        @Part("InvoiceNumbers[]") InvoiceNumbers: ArrayList<RequestBody>,
        @Part imageFile: MultipartBody.Part? = null,   // optional
    ): Call<JsonObject>

    @PUT(Constants.API_Update_Customer)
    fun updateCustomer(@Body updateCustomerRequest: CustomerUpdate): Call<JsonObject>

    @POST(Constants.API_Order_Post)
    fun orderPost(@Body orderPostRequest: OrderPostRequest): Call<JsonObject>

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


    @Multipart
    @POST(Constants.API_Confirm_DriverTrip)
    fun driverTripConfirm(
        @Part("BCOrderNo") BCOrderNo: RequestBody,
        @Part("Remarks") Remarks: RequestBody,
        @Part("CustomerGeoLocation") CustomerGeoLocation: RequestBody,
        @Part signatureImage: MultipartBody.Part?, // <-- multiple files
        @Part deliveryPhoto: MultipartBody.Part? // <-- multiple files
    ): Call<JsonObject>

    @GET
    suspend fun getDropdownData(@Url url: String): Response<JsonElement>


}