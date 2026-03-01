package com.gpsjammingdetector.domain.model

data class DetectionConfig(
    val maxSpeedKmh: Double = 300.0,
    val teleportationDistanceMeters: Double = 5000.0,
    val altitudeSpikeMeters: Double = 500.0,
    val gpsUpdateIntervalMs: Long = 2000L
)
