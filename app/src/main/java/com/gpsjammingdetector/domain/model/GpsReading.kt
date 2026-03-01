package com.gpsjammingdetector.domain.model

data class GpsReading(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val speed: Float,
    val bearing: Float,
    val timestamp: Long,
    val isMock: Boolean,
    val provider: String,
    val satelliteCount: Int,
    val sessionId: String
)
