package com.alvimatruck.utils

class Constants {
    companion object {

        // Base URL
        const val BASE_URL = "http://192.168.1.148:8696/api/" //Sandbox
        const val IMAGE_URL = "http://192.168.1.148:8696/" //Sandbox

        const val API_SignIn = "MobileLogin/mobilelogin"
        const val API_ResetPassword = "MobileLogin/reset-password"
        const val API_ResendOTP = "MobileLogin/resend-otp"
        const val API_OTPVerify = "MobileLogin/verify-otp"
        const val API_ChangePassword = "MobileLogin/change-password"

        const val API_VanList = "Dropdown/get-van-sales-dropdown"
        const val API_Price_Group = "Dropdown/get-customer-price-group-dropdown"
        const val API_City = "Dropdown/get-postcodes-dropdown"
        const val API_Location_Code = "Dropdown/get-locations-dropdown"
        const val API_Payment_Code = "Dropdown/get-payment-methods-dropdown"
        const val API_Item_List = "Dropdown/items"
        const val API_Route_Cancel_Reason_List = "Dropdown/get-cancel-reasons"
        const val API_Visit_Reason_List = "Dropdown/get-visit-reasons"
        const val API_CostCenter_Code = "Dropdown/get-costcenter-dropdown"
        const val API_ProfitCenter_Code = "Dropdown/get-profitcenter-dropdown"
        const val API_Intransit_Code = "Dropdown/get-intransit-dropdown"

        const val API_Create_Customer = "Customer/create-customer"
        const val API_Update_Customer = "Customer/update"
        const val API_Customer_List = "Customer/getallmobile"
        const val API_Customer_Price = "Customer/get-price"

        const val API_Today_Routes = "MobileRoutes/today-routes"

        const val API_Sales_Orders = "SalesOrder/get-all"
        const val API_New_Order = "SalesOrder/create"
        const val API_Order_Detail = "SalesOrder/get-by-id"
        const val API_Van_Stock = "SalesOrder/get-stock-by-salesperson"

        const val API_Start_Trip = "Trip/start"
        const val API_End_Trip = "Trip/end"
        const val API_Visit_Trip = "Trip/visit"
        const val API_Cancel_Trip = "Trip/cancel"

        const val API_Fleet_Log_List = "FleetManagement/get-all-fleet-mobile"
        const val API_Fleet = "FleetManagement/create-fleat"


        // Constants Key
        const val IS_LOGIN = "is_login"
        const val IS_Salesperson = "is_salesperson"
        const val VanNo = "van_no"
        const val Username = "username"
        const val UserDetail = "userdetails"

        const val Token = "token"

        const val Password = "password"
        const val RememberMe = "remember_me"

        const val FingerPrintEnabled = "fingerprint_enabled"

        const val Status = "status"
        const val IS_HIDE = "is_hide"

        const val RouteDetail = "route_detail"
        const val TripStart = "trip_start"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val CustomerDetail = "customer_detail"
        const val OrderID = "order_id"

    }


}