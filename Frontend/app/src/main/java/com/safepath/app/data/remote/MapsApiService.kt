package com.safepath.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Google Directions API.
 * Base URL: https://maps.googleapis.com/maps/api/
 */
interface MapsApiService {

    /**
     * Fetch up to [alternatives] routes between [origin] and [destination].
     */
    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin")       origin: String,
        @Query("destination")  destination: String,
        @Query("alternatives") alternatives: Boolean = true,
        @Query("mode")         mode: String = "walking",
        @Query("key")          apiKey: String
    ): DirectionsResponse
}
