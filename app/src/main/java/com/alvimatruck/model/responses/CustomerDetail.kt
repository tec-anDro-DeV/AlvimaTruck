package com.alvimatruck.model.responses

data class CustomerDetail(
    var address: String,
    val address2: String?,
    var city: String,
    val contact: String,
    val creditLimitLcy: Double,
    val customerImage: String?,
    val customerPostingGroup: String?,
    val customerPriceGroup: String?,
    val idProof: String?,
    var latitude: Double,
    val locationCode: String?,
    var longitude: Double,
    val no: String,
    val phoneNo: String?,
    val routeName: String,
    val searchName: String,
    var postCode: String,
    val status: String,
    var telexNo: String?,
    val tinNo: String?,
    val balanceLcy: Double,
    var visitedToday: Boolean
) {
    fun getFormattedContactNo(): String {
        val number = phoneNo?.trim()
        if (number.isNullOrEmpty()) return "-"

        return if (number.startsWith("0")) {
            "+251 " + number.substring(1)
        } else {
            "+251 $number"
        }
    }

    fun getFormattedTelephoneNo(): String {
        val number = telexNo?.trim()
        if (number.isNullOrEmpty()) return "-"

        return if (number.startsWith("0")) {
            "+251 " + number.substring(1)
        } else {
            "+251 $number"
        }
    }


}