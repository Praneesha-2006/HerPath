package com.safepath.app.data.remote

import com.google.gson.annotations.SerializedName

// ─── Top-level response ───────────────────────────────────────────────────────

data class DirectionsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("routes") val routes: List<DirectionsRoute>
)

// ─── Route ───────────────────────────────────────────────────────────────────

data class DirectionsRoute(
    @SerializedName("summary")          val summary: String,
    @SerializedName("legs")             val legs: List<Leg>,
    @SerializedName("overview_polyline") val overviewPolyline: OverviewPolyline
)

// ─── Leg ─────────────────────────────────────────────────────────────────────

data class Leg(
    @SerializedName("distance")          val distance: TextValue,
    @SerializedName("duration")          val duration: TextValue,
    @SerializedName("steps")             val steps: List<Step>,
    @SerializedName("start_location")    val startLocation: Location,
    @SerializedName("end_location")      val endLocation: Location
)

// ─── Step ────────────────────────────────────────────────────────────────────

data class Step(
    @SerializedName("html_instructions") val htmlInstructions: String,
    @SerializedName("distance")          val distance: TextValue,
    @SerializedName("duration")          val duration: TextValue,
    @SerializedName("start_location")    val startLocation: Location,
    @SerializedName("end_location")      val endLocation: Location,
    @SerializedName("maneuver")          val maneuver: String? = null,
    @SerializedName("polyline")          val polyline: OverviewPolyline
)

// ─── Helpers ─────────────────────────────────────────────────────────────────

data class TextValue(
    @SerializedName("text")  val text: String,
    @SerializedName("value") val value: Int
)

data class Location(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

data class OverviewPolyline(
    @SerializedName("points") val points: String
)
