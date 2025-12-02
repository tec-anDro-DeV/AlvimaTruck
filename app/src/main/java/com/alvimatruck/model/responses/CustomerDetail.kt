package com.alvimatruck.model.responses

data class CustomerDetail(
    val address: String,
    val address2: String?,
    val city: String,
    val contact: String,
    val creditLimitLcy: Int,
    val customerImage: String?,
    val customerPostingGroup: String?,
    val customerPriceGroup: String?,
    val idProof: String?,
    val latitude: Double,
    val locationCode: String?,
    val longitude: Double,
    val no: String,
    val phoneNo: String?,
    val routeName: String,
    val searchName: String,
    val status: String,
    val telexNo: String?,
    val tinNo: String?
) {
    fun getFormattedContactNo(): String {
        val number = phoneNo ?: return ""
        return if (number.startsWith("0")) {
            "+251 " + number.substring(1)
        } else {
            "+251 $number"
        }
    }

    fun getFormattedTelephoneNo(): String {
        val number = telexNo ?: return ""
        return if (number.startsWith("0")) {
            "+251 " + number.substring(1)
        } else {
            "+251 $number"
        }
    }


}