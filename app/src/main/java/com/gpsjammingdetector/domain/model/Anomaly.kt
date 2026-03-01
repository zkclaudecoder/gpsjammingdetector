package com.gpsjammingdetector.domain.model

data class Anomaly(
    val type: AnomalyType,
    val severity: Severity,
    val description: String,
    val value: Double,
    val threshold: Double,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val sessionId: String
)
