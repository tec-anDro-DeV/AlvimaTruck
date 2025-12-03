package com.alvimatruck.utils

class Constants {
    companion object {

        // Base URL
        const val BASE_URL = "http://192.168.1.148:8696/api/" //Sandbox
        const val IMAGE_URL = "http://192.168.1.148:8696/" //Sandbox

        const val API_SignIn = "MobileLogin/mobilelogin"
        const val API_VanList = "Dropdown/get-van-sales-dropdown"
        const val API_ResetPassword = "MobileLogin/reset-password"
        const val API_ResendOTP = "MobileLogin/resend-otp"
        const val API_OTPVerify = "MobileLogin/verify-otp"
        const val API_ChangePassword = "MobileLogin/change-password"

        const val API_Price_Group = "Dropdown/get-customer-price-group-dropdown"
        const val API_Location_Code = "Dropdown/get-locations-dropdown"
        const val API_Payment_Code = "Dropdown/get-payment-methods-dropdown"
        const val API_Item_List = "Dropdown/items"

        const val API_Create_Customer = "Customer/create-customer"
        const val API_Today_Routes = "MobileRoutes/today-routes"
        const val API_Customer_List = "Customer/getallmobile"


        // Constants Key
        const val IS_LOGIN = "is_login"
        const val IS_SYNC = "is_sync"
        const val VanNo = "van_no"
        const val Username = "username"
        const val UserDetail = "userdetails"

        const val Token = "token"

        const val Password = "password"
        const val RememberMe = "remember_me"

        const val FingerPrintEnabled = "fingerprint_enabled"

        const val Status = "status"

        const val RouteDetail = "route_detail"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val CustomerDetail = "customer_detail"
    }


}