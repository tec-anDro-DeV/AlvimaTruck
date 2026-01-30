package com.alvimatruck.interfaces

import com.alvimatruck.model.responses.DeliveryTripDetail

interface DeliveryClickListener {
    fun onDeliveryClick(deliveryTripDetail: DeliveryTripDetail)
}