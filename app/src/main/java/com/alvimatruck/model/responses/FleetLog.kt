package com.alvimatruck.model.responses

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
            fuelRefillAmount.toString()
        } else {
            repairLogRepairCost.toString()
        }
    }

    fun getRequestDate(): String {
        return LocalDateTime.parse(
            createdDate.padEnd(createdDate.length + (23 - createdDate.length), '0'),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        ).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}