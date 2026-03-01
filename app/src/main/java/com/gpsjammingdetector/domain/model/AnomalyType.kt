package com.gpsjammingdetector.domain.model

enum class AnomalyType(val displayName: String) {
    TELEPORTATION("Teleportation"),
    IMPOSSIBLE_SPEED("Impossible Speed"),
    ALTITUDE_SPIKE("Altitude Spike"),
    MOCK_LOCATION("Mock Location")
}
