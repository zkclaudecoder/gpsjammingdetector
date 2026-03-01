package com.gpsjammingdetector.domain.detection

import com.gpsjammingdetector.domain.model.Anomaly
import com.gpsjammingdetector.domain.model.AnomalyType
import com.gpsjammingdetector.domain.model.DetectionConfig
import com.gpsjammingdetector.domain.model.GpsReading
import com.gpsjammingdetector.domain.model.Severity
import kotlin.math.abs

class AnomalyDetector(private val config: DetectionConfig) {

    /**
     * Detects anomalies between the current reading and the previous one.
     * Returns a list of detected anomalies (can be empty, or multiple for one fix).
     */
    fun detect(current: GpsReading, previous: GpsReading?): List<Anomaly> {
        val anomalies = mutableListOf<Anomaly>()

        // Mock location check — doesn't need a previous reading
        if (current.isMock) {
            anomalies += Anomaly(
                type = AnomalyType.MOCK_LOCATION,
                severity = Severity.CRITICAL,
                description = "Mock location provider detected",
                value = 1.0,
                threshold = 0.0,
                latitude = current.latitude,
                longitude = current.longitude,
                timestamp = current.timestamp,
                sessionId = current.sessionId
            )
        }

        // The remaining checks require a previous reading
        if (previous == null) return anomalies

        val distance = SpeedCalculator.haversineDistance(
            previous.latitude, previous.longitude,
            current.latitude, current.longitude
        )
        val timeDelta = current.timestamp - previous.timestamp
        val speedKmh = SpeedCalculator.computeSpeedKmh(distance, timeDelta)
        val altitudeDelta = abs(current.altitude - previous.altitude)

        // Teleportation check
        if (distance > config.teleportationDistanceMeters) {
            anomalies += Anomaly(
                type = AnomalyType.TELEPORTATION,
                severity = computeSeverity(distance, config.teleportationDistanceMeters),
                description = "Position jumped %.0fm (threshold: %.0fm)".format(distance, config.teleportationDistanceMeters),
                value = distance,
                threshold = config.teleportationDistanceMeters,
                latitude = current.latitude,
                longitude = current.longitude,
                timestamp = current.timestamp,
                sessionId = current.sessionId
            )
        }

        // Impossible speed check
        if (speedKmh > config.maxSpeedKmh) {
            anomalies += Anomaly(
                type = AnomalyType.IMPOSSIBLE_SPEED,
                severity = computeSeverity(speedKmh, config.maxSpeedKmh),
                description = "Computed speed %.0f km/h (threshold: %.0f km/h)".format(speedKmh, config.maxSpeedKmh),
                value = speedKmh,
                threshold = config.maxSpeedKmh,
                latitude = current.latitude,
                longitude = current.longitude,
                timestamp = current.timestamp,
                sessionId = current.sessionId
            )
        }

        // Altitude spike check
        if (altitudeDelta > config.altitudeSpikeMeters) {
            anomalies += Anomaly(
                type = AnomalyType.ALTITUDE_SPIKE,
                severity = computeSeverity(altitudeDelta, config.altitudeSpikeMeters),
                description = "Altitude changed %.0fm (threshold: %.0fm)".format(altitudeDelta, config.altitudeSpikeMeters),
                value = altitudeDelta,
                threshold = config.altitudeSpikeMeters,
                latitude = current.latitude,
                longitude = current.longitude,
                timestamp = current.timestamp,
                sessionId = current.sessionId
            )
        }

        return anomalies
    }

    companion object {
        fun computeSeverity(value: Double, threshold: Double): Severity {
            val ratio = value / threshold
            return when {
                ratio > 10 -> Severity.CRITICAL
                ratio > 5 -> Severity.HIGH
                ratio > 2 -> Severity.MEDIUM
                else -> Severity.LOW
            }
        }
    }
}
