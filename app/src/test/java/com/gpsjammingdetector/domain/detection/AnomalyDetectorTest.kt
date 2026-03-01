package com.gpsjammingdetector.domain.detection

import com.gpsjammingdetector.domain.model.AnomalyType
import com.gpsjammingdetector.domain.model.DetectionConfig
import com.gpsjammingdetector.domain.model.GpsReading
import com.gpsjammingdetector.domain.model.Severity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnomalyDetectorTest {

    private lateinit var detector: AnomalyDetector
    private val baseReading = GpsReading(
        latitude = 40.7128,
        longitude = -74.0060,
        altitude = 10.0,
        accuracy = 5.0f,
        speed = 0f,
        bearing = 0f,
        timestamp = 1000000L,
        isMock = false,
        provider = "gps",
        satelliteCount = 12,
        sessionId = "test-session"
    )

    @Before
    fun setup() {
        detector = AnomalyDetector(DetectionConfig())
    }

    @Test
    fun `no anomalies for normal sequential readings`() {
        val previous = baseReading
        val current = baseReading.copy(
            latitude = 40.7129, // ~11 meters
            timestamp = 1002000L // 2 seconds later
        )

        val anomalies = detector.detect(current, previous)
        assertTrue("Expected no anomalies, got: $anomalies", anomalies.isEmpty())
    }

    @Test
    fun `no anomalies for first reading without previous`() {
        val anomalies = detector.detect(baseReading, null)
        assertTrue(anomalies.isEmpty())
    }

    @Test
    fun `detects mock location`() {
        val mockReading = baseReading.copy(isMock = true)
        val anomalies = detector.detect(mockReading, null)

        assertEquals(1, anomalies.size)
        assertEquals(AnomalyType.MOCK_LOCATION, anomalies[0].type)
        assertEquals(Severity.CRITICAL, anomalies[0].severity)
    }

    @Test
    fun `detects teleportation`() {
        val previous = baseReading
        val current = baseReading.copy(
            latitude = 41.7128, // ~111 km jump
            timestamp = 1002000L // 2 seconds later
        )

        val anomalies = detector.detect(current, previous)
        val teleportation = anomalies.find { it.type == AnomalyType.TELEPORTATION }

        assertTrue("Expected teleportation anomaly", teleportation != null)
    }

    @Test
    fun `detects impossible speed`() {
        val previous = baseReading
        val current = baseReading.copy(
            latitude = 40.8128, // ~11.1 km
            timestamp = 1002000L // 2 seconds later → ~20,000 km/h
        )

        val anomalies = detector.detect(current, previous)
        val speedAnomaly = anomalies.find { it.type == AnomalyType.IMPOSSIBLE_SPEED }

        assertTrue("Expected impossible speed anomaly", speedAnomaly != null)
    }

    @Test
    fun `detects altitude spike`() {
        val previous = baseReading
        val current = baseReading.copy(
            altitude = 1000.0, // 990m jump
            timestamp = 1002000L
        )

        val anomalies = detector.detect(current, previous)
        val altitudeAnomaly = anomalies.find { it.type == AnomalyType.ALTITUDE_SPIKE }

        assertTrue("Expected altitude spike anomaly", altitudeAnomaly != null)
    }

    @Test
    fun `severity LOW for values near threshold`() {
        val severity = AnomalyDetector.computeSeverity(5500.0, 5000.0) // 1.1x
        assertEquals(Severity.LOW, severity)
    }

    @Test
    fun `severity MEDIUM for values 2-5x threshold`() {
        val severity = AnomalyDetector.computeSeverity(15000.0, 5000.0) // 3x
        assertEquals(Severity.MEDIUM, severity)
    }

    @Test
    fun `severity HIGH for values 5-10x threshold`() {
        val severity = AnomalyDetector.computeSeverity(35000.0, 5000.0) // 7x
        assertEquals(Severity.HIGH, severity)
    }

    @Test
    fun `severity CRITICAL for values over 10x threshold`() {
        val severity = AnomalyDetector.computeSeverity(55000.0, 5000.0) // 11x
        assertEquals(Severity.CRITICAL, severity)
    }

    @Test
    fun `detects multiple anomalies simultaneously`() {
        val previous = baseReading
        val current = baseReading.copy(
            latitude = 41.7128, // teleportation + impossible speed
            altitude = 1000.0, // altitude spike
            isMock = true, // mock location
            timestamp = 1002000L
        )

        val anomalies = detector.detect(current, previous)
        assertTrue("Expected at least 3 anomalies, got ${anomalies.size}", anomalies.size >= 3)

        val types = anomalies.map { it.type }.toSet()
        assertTrue(AnomalyType.MOCK_LOCATION in types)
        assertTrue(AnomalyType.TELEPORTATION in types)
        assertTrue(AnomalyType.ALTITUDE_SPIKE in types)
    }
}
