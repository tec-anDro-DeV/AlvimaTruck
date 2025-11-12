package com.alvimatruck.utils

class Constants {
    companion object {

        // Base URL
        const val BASE_URL =
            "http://192.168.1.148:8696/api/" //Sandbox

        const val API_SignIn = "MobileLogin/mobilelogin"
        const val API_VanList = "Dropdown/get-van-sales-dropdown"
        const val API_ResetPassword = "MobileLogin/reset-password"
        const val API_ResendOTP = "MobileLogin/resend-otp"
        const val API_OTPVerify = "MobileLogin/verify-otp"
        const val API_ChangePassword = "MobileLogin/change-password"


        // Constants Key
        const val IS_LOGIN = "is_login"
        const val VanNo = "van_no"
        const val Username = "username"
        const val UserDetail = "userdetails"

        const val Token = "token"

        const val Password = "password"
        const val RememberMe = "remember_me"

        const val FingerPrintEnabled = "fingerprint_enabled"

        const val Status = "status"

    }


}