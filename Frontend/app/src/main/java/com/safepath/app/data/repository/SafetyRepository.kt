package com.safepath.app.data.repository

import com.safepath.app.data.model.SafetyLevel
import com.safepath.app.data.model.SafetyScore
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Computes a safety score (0-100) for a given route based on multiple signals.
 *
 * Algorithm:
 *   base score = 70
 *   ± time-of-day penalty   (night = -20, late evening = -10)
 *   ± distance bonus/penalty (short routes preferred for safety)
 *   ± mock POI density bonus (simulated well-lit/crowded areas)
 */
@Singleton
class SafetyRepository @Inject constructor() {

    /**
     * Returns a [SafetyScore] for the given route index and travel distance in metres.
     * The [routeIndex] is used to inject variation across alternate routes.
     */
    fun computeScore(routeIndex: Int, distanceMeters: Int): SafetyScore {
        var score = 70.0

        // 1. Time-of-day factor
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val (timePenalty, timeReason) = when (hour) {
            in 6..11  -> Pair(5,   "Morning hours — high foot traffic")
            in 12..17 -> Pair(5,   "Daytime — well-populated streets")
            in 18..20 -> Pair(-5,  "Evening — moderate visibility")
            in 21..22 -> Pair(-15, "Night — reduced street activity")
            else       -> Pair(-20, "Late night — use extra caution")
        }
        score += timePenalty

        // 2. Route-specific safety variation (simulating POI / lighting data)
        val (routeBonus, safetyReasons) = when (routeIndex) {
            0 -> Pair(15.0, listOf(
                "Well-lit main road",
                "High CCTV coverage",
                "Busy commercial area",
                timeReason
            ))
            1 -> Pair(0.0, listOf(
                "Moderate street lighting",
                "Some CCTV cameras",
                "Mixed residential/commercial",
                timeReason
            ))
            else -> Pair(-20.0, listOf(
                "Poorly lit back streets",
                "No CCTV coverage",
                "Low foot traffic",
                timeReason
            ))
        }
        score += routeBonus

        // 3. Distance penalty (very long routes add fatigue/risk)
        if (distanceMeters > 5000) score -= 5
        if (distanceMeters > 10000) score -= 5

        val finalScore = score.roundToInt().coerceIn(0, 100)
        return SafetyScore.fromScore(finalScore, safetyReasons)
    }

    fun safetyLevelColor(level: SafetyLevel): Long = when (level) {
        SafetyLevel.SAFE     -> 0xFF00E676  // Emerald green
        SafetyLevel.MODERATE -> 0xFFFFB300  // Amber
        SafetyLevel.RISKY    -> 0xFFFF1744  // Red
    }
}
