package com.alvimatruck.model.responses

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

data class FleetLog(
    val createdDate: String,
    val fleetType: String,
    val fuelRefillAmount: Double,
    val incidentReportDescription: String,
    val incidentReportType: String,
    val reason: String,
    val repairLogRepairCost: Double,
    val repairLogVendorDetail: String,
    val status: String,
    val uniqueId: Int
) {
    fun getCost(): String {
        return if (fleetType == "FuleRefill") {
            "ETB $fuelRefillAmount"
        } else {
            "ETB $repairLogRepairCost"
        }
    }

    fun getRequestDate(): String {
        return LocalDateTime.parse(
            createdDate, DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                .optionalEnd()
                .toFormatter()
        ).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}