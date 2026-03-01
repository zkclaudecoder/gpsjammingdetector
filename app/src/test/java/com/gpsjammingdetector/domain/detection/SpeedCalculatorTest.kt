package com.gpsjammingdetector.domain.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeedCalculatorTest {

    @Test
    fun `haversine distance between same point is zero`() {
        val distance = SpeedCalculator.haversineDistance(40.7128, -74.0060, 40.7128, -74.0060)
        assertEquals(0.0, distance, 0.01)
    }

    @Test
    fun `haversine distance NYC to LA is approximately correct`() {
        // NYC to LA is roughly 3,944 km
        val distance = SpeedCalculator.haversineDistance(
            40.7128, -74.0060, // NYC
            34.0522, -118.2437 // LA
        )
        val distanceKm = distance / 1000.0
        assertTrue("Expected ~3944 km, got $distanceKm km", distanceKm > 3900 && distanceKm < 4000)
    }

    @Test
    fun `haversine distance short distance is accurate`() {
        // ~111 km per degree of latitude at equator
        val distance = SpeedCalculator.haversineDistance(0.0, 0.0, 1.0, 0.0)
        val distanceKm = distance / 1000.0
        assertTrue("Expected ~111 km, got $distanceKm km", distanceKm > 110 && distanceKm < 112)
    }

    @Test
    fun `compute speed returns correct value`() {
        // 1000 meters in 1 second = 3600 km/h
        val speed = SpeedCalculator.computeSpeedKmh(1000.0, 1000)
        assertEquals(3600.0, speed, 0.01)
    }

    @Test
    fun `compute speed returns zero for zero time delta`() {
        val speed = SpeedCalculator.computeSpeedKmh(1000.0, 0)
        assertEquals(0.0, speed, 0.01)
    }

    @Test
    fun `compute speed returns zero for negative time delta`() {
        val speed = SpeedCalculator.computeSpeedKmh(1000.0, -100)
        assertEquals(0.0, speed, 0.01)
    }
}
