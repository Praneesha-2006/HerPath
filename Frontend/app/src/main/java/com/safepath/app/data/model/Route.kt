package com.safepath.app.data.model

import com.google.android.gms.maps.model.LatLng

data class Route(
    val id: Int,
    val name: String,
    val distance: String,
    val duration: String,
    val safetyScore: SafetyScore,
    val polylinePoints: List<LatLng>,
    val steps: List<NavigationStep>,
    val isRecommended: Boolean = false,
    val highlights: List<String> = emptyList()
)
