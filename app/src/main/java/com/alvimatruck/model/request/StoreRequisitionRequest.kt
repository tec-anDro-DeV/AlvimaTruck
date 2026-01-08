package com.alvimatruck.model.request

data class StoreRequisitionRequest(
    val costCenter: String,
    val fromLocation: String,
    val inTransitCode: String,
    val lines: List<StoreRequisitionLine>,
    val profitCenter: String,
    val toLocation: String
)

data class StoreRequisitionLine(
    val fromLocation: String,
    val itemNo: String,
    val quantityRequested: Int
)