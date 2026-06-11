package com.safepath.app.data.repository

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.safepath.app.BuildConfig
import com.safepath.app.data.model.NavigationStep
import com.safepath.app.data.model.Route
import com.safepath.app.data.remote.DirectionsResponse
import com.safepath.app.data.remote.MapsApiService
import com.safepath.app.util.PolylineDecoder
import javax.inject.Inject
import javax.inject.Singleton

sealed class RouteResult {
    data class Success(val routes: List<Route>) : RouteResult()
    data class Error(val message: String) : RouteResult()
}

@Singleton
class RouteRepository @Inject constructor(
    private val mapsApiService: MapsApiService,
    private val safetyRepository: SafetyRepository
) {
    private val apiKey = BuildConfig.MAPS_API_KEY

    /**
     * Fetch up to 3 routes. Falls back to mock data if API key is empty or call fails.
     */
    suspend fun getRoutes(source: String, destination: String): RouteResult {
        if (apiKey.isBlank() || apiKey == "YOUR_MAPS_API_KEY_HERE") {
            return RouteResult.Success(getMockRoutes(source, destination))
        }
        return try {
            val response = mapsApiService.getDirections(
                origin      = source,
                destination = destination,
                apiKey      = apiKey
            )
            if (response.status == "OK") {
                RouteResult.Success(mapToRoutes(response))
            } else {
                Log.w("RouteRepository", "API returned: ${response.status}. Using mock.")
                RouteResult.Success(getMockRoutes(source, destination))
            }
        } catch (e: Exception) {
            Log.e("RouteRepository", "API error: ${e.message}. Using mock.")
            RouteResult.Success(getMockRoutes(source, destination))
        }
    }

    // ─── API → Domain mapping ────────────────────────────────────────────────

    private fun mapToRoutes(response: DirectionsResponse): List<Route> {
        return response.routes.take(3).mapIndexed { index, apiRoute ->
            val leg = apiRoute.legs.first()
            val distanceMeters = leg.distance.value

            val steps = leg.steps.map { step ->
                NavigationStep(
                    instruction  = step.htmlInstructions.stripHtml(),
                    distance     = step.distance.text,
                    duration     = step.duration.text,
                    startLocation = LatLng(step.startLocation.lat, step.startLocation.lng),
                    endLocation   = LatLng(step.endLocation.lat, step.endLocation.lng),
                    maneuver     = step.maneuver ?: "straight"
                )
            }

            val polyline = PolylineDecoder.decode(apiRoute.overviewPolyline.points)
            val safety   = safetyRepository.computeScore(index, distanceMeters)

            Route(
                id            = index,
                name          = routeLabel(index),
                distance      = leg.distance.text,
                duration      = leg.duration.text,
                safetyScore   = safety,
                polylinePoints = polyline,
                steps         = steps,
                isRecommended = index == 0
            )
        }
    }

    // ─── Mock data ───────────────────────────────────────────────────────────

    fun getMockRoutes(source: String, destination: String): List<Route> {
        // Central Delhi coordinates as demo
        val base = LatLng(28.6304, 77.2177)

        return listOf(
            // Route 0 — Safe (Recommended)
            Route(
                id = 0,
                name = "Via Janpath (Recommended)",
                distance = "3.2 km",
                duration = "12 min",
                safetyScore = safetyRepository.computeScore(0, 3200),
                polylinePoints = listOf(
                    LatLng(28.6304, 77.2177),
                    LatLng(28.6289, 77.2198),
                    LatLng(28.6270, 77.2220),
                    LatLng(28.6250, 77.2255),
                    LatLng(28.6236, 77.2291)
                ),
                steps = listOf(
                    NavigationStep("Head south on Janpath", "0.5 km", "2 min",
                        LatLng(28.6304, 77.2177), LatLng(28.6270, 77.2177), "straight"),
                    NavigationStep("Turn left onto Rajpath", "1.2 km", "5 min",
                        LatLng(28.6270, 77.2177), LatLng(28.6236, 77.2255), "turn-left"),
                    NavigationStep("Turn right at India Gate", "1.5 km", "5 min",
                        LatLng(28.6236, 77.2255), LatLng(28.6236, 77.2291), "turn-right"),
                    NavigationStep("Arrive at destination", "0 m", "0 min",
                        LatLng(28.6236, 77.2291), LatLng(28.6236, 77.2291), "arrive")
                ),
                isRecommended = true,
                highlights = listOf("CCTV Zone", "Metro Nearby", "Well-lit")
            ),

            // Route 1 — Moderate
            Route(
                id = 1,
                name = "Via Kasturba Gandhi Marg",
                distance = "4.1 km",
                duration = "18 min",
                safetyScore = safetyRepository.computeScore(1, 4100),
                polylinePoints = listOf(
                    LatLng(28.6304, 77.2177),
                    LatLng(28.6320, 77.2210),
                    LatLng(28.6298, 77.2260),
                    LatLng(28.6260, 77.2280),
                    LatLng(28.6236, 77.2291)
                ),
                steps = listOf(
                    NavigationStep("Head north on Barakhamba Road", "0.8 km", "3 min",
                        LatLng(28.6304, 77.2177), LatLng(28.6320, 77.2210), "straight"),
                    NavigationStep("Turn right on KG Marg", "1.8 km", "8 min",
                        LatLng(28.6320, 77.2210), LatLng(28.6298, 77.2260), "turn-right"),
                    NavigationStep("Continue to Tilak Marg", "1.5 km", "7 min",
                        LatLng(28.6298, 77.2260), LatLng(28.6236, 77.2291), "straight"),
                    NavigationStep("Arrive at destination", "0 m", "0 min",
                        LatLng(28.6236, 77.2291), LatLng(28.6236, 77.2291), "arrive")
                ),
                isRecommended = false,
                highlights = listOf("Bus Stop Nearby", "Some Lighting")
            ),

            // Route 2 — Risky
            Route(
                id = 2,
                name = "Via Minto Road (Shorter)",
                distance = "2.8 km",
                duration = "10 min",
                safetyScore = safetyRepository.computeScore(2, 2800),
                polylinePoints = listOf(
                    LatLng(28.6304, 77.2177),
                    LatLng(28.6285, 77.2155),
                    LatLng(28.6260, 77.2160),
                    LatLng(28.6240, 77.2200),
                    LatLng(28.6236, 77.2291)
                ),
                steps = listOf(
                    NavigationStep("Head south on Minto Road", "0.6 km", "2 min",
                        LatLng(28.6304, 77.2177), LatLng(28.6285, 77.2155), "straight"),
                    NavigationStep("Turn left onto Sher Shah Road", "1.2 km", "4 min",
                        LatLng(28.6285, 77.2155), LatLng(28.6240, 77.2200), "turn-left"),
                    NavigationStep("Continue to C-Hexagon", "1.0 km", "4 min",
                        LatLng(28.6240, 77.2200), LatLng(28.6236, 77.2291), "straight"),
                    NavigationStep("Arrive at destination", "0 m", "0 min",
                        LatLng(28.6236, 77.2291), LatLng(28.6236, 77.2291), "arrive")
                ),
                isRecommended = false,
                highlights = listOf("Isolated", "No CCTV")
            )
        )
    }

    private fun routeLabel(index: Int) = when (index) {
        0    -> "Safest Route"
        1    -> "Alternative Route"
        else -> "Fastest Route"
    }

    private fun String.stripHtml() = replace(Regex("<[^>]*>"), "").trim()
}
