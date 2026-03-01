package com.gpsjammingdetector.domain.detection

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object SpeedCalculator {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    /**
     * Computes the Haversine distance in meters between two lat/lon points.
     */
    fun haversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Computes speed in km/h given distance in meters and time delta in milliseconds.
     * Returns 0 if timeDeltaMs is zero or negative.
     */
    fun computeSpeedKmh(distanceMeters: Double, timeDeltaMs: Long): Double {
        if (timeDeltaMs <= 0) return 0.0
        val timeHours = timeDeltaMs / 3_600_000.0
        return (distanceMeters / 1000.0) / timeHours
    }
}
