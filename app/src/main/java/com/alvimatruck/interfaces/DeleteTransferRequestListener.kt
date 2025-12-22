package com.alvimatruck.interfaces

import com.alvimatruck.model.responses.SingleTransfer

interface DeleteTransferRequestListener {
    fun onDeleteRequest(singleTransfer: SingleTransfer)
}