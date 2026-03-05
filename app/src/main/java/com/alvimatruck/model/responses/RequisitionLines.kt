package com.alvimatruck.model.responses

data class RequisitionLines(
    val description: String,
    val documentType: String,
    val fromLocation: String,
    val lineNo: Int,
    val no: String,
    val quantityRequested: Int,
    val requisitionNo: String,
    val type: String,
    val unitOfMeasure: String = ""
)