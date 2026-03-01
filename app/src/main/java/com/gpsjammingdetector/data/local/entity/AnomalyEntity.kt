package com.gpsjammingdetector.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "anomalies",
    foreignKeys = [
        ForeignKey(
            entity = GpsReadingEntity::class,
            parentColumns = ["id"],
            childColumns = ["readingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("readingId"), Index("sessionId")]
)
data class AnomalyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val readingId: Long,
    val type: String,
    val severity: String,
    val description: String,
    val value: Double,
    val threshold: Double,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val sessionId: String
)
