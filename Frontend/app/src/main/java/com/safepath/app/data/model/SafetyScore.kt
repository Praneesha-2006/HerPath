package com.safepath.app.data.model

enum class SafetyLevel(val label: String) {
    SAFE("Safe"),
    MODERATE("Moderate"),
    RISKY("Risky")
}

data class SafetyScore(
    val score: Int,          // 0–100
    val level: SafetyLevel,
    val reasons: List<String>
) {
    companion object {
        fun fromScore(score: Int, reasons: List<String> = emptyList()): SafetyScore {
            val level = when {
                score >= 80 -> SafetyLevel.SAFE
                score >= 50 -> SafetyLevel.MODERATE
                else        -> SafetyLevel.RISKY
            }
            return SafetyScore(score, level, reasons)
        }
    }
}
