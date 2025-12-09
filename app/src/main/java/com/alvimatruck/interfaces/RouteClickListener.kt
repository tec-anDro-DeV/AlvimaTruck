package com.alvimatruck.interfaces

import com.alvimatruck.model.responses.RouteDetail

interface RouteClickListener {
    fun onRouteClick(routeDetail: RouteDetail)
}