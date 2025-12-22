package com.alvimatruck.model.request

import com.alvimatruck.model.responses.SingleTransfer

data class TransferRequest(
    val costCenter: String,
    val inTransitCode: String,
    val lines: List<SingleTransfer>,
    val profitCenter: String,
    val transferFromCode: String,
    val transferToCode: String
)