package com.mslabs.aegis.presentation.components

data class TotpRing(
    val secondsRemaining: Int,
    val totalSeconds: Int = 30,
) {
    val progress: Float = secondsRemaining.coerceIn(0, totalSeconds) / totalSeconds.toFloat()
}
