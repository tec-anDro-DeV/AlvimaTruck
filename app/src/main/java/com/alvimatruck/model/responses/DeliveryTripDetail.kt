package com.alvimatruck.model.responses

data class DeliveryTripDetail(
    val customerGEOLocation: String,
    val deliveryDateTime: String,
    val deliveryStatus: String,
    val driverID: String,
    val latitude: Double,
    val longitude: Double,
    val no: String,
    val orderNo: String,
    val postedSalesShipmentLines: ArrayList<PostedSalesShipmentLine>,
    val sellToCustomerName: String,
    val sellToCustomerNo: String,
    val shipToAddress: String,
    val shipmentDate: String
)

data class PostedSalesShipmentLine(
    val description: String,
    val documentNo: String,
    val lineNo: Int,
    val no: String,
    val quantity: Int,
    val systemId: String,
    val type: String,
    val unitOfMeasure: String
)