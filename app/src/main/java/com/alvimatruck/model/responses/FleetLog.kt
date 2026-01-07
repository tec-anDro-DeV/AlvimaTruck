package com.alvimatruck.model.responses

import com.alvimatruck.utils.Utils

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
        return Utils.getFormatedRequestDate(createdDate)
    }
}