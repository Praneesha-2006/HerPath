package com.safepath.app.data.model

import com.google.android.gms.maps.model.LatLng

data class NavigationStep(
    val instruction: String,
    val distance: String,
    val duration: String,
    val startLocation: LatLng,
    val endLocation: LatLng,
    val maneuver: String = "straight"   // "turn-left", "turn-right", "straight", "arrive"
)
