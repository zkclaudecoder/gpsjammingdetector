package com.gpsjammingdetector.data.repository

import com.gpsjammingdetector.data.local.dao.AnomalyDao
import com.gpsjammingdetector.data.local.dao.GpsReadingDao
import com.gpsjammingdetector.data.local.entity.AnomalyEntity
import com.gpsjammingdetector.data.local.entity.GpsReadingEntity
import com.gpsjammingdetector.domain.detection.AnomalyDetector
import com.gpsjammingdetector.domain.model.Anomaly
import com.gpsjammingdetector.domain.model.GpsReading
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpsRepositoryImpl @Inject constructor(
    private val gpsReadingDao: GpsReadingDao,
    private val anomalyDao: AnomalyDao,
    private val anomalyDetector: AnomalyDetector
) : GpsRepository {

    override suspend fun processNewReading(reading: GpsReading): List<Anomaly> {
        // Get the previous reading for this session
        val previousEntity = gpsReadingDao.getLatestReadingForSession(reading.sessionId)
        val previous = previousEntity?.toDomain()

        // Insert the new reading
        val readingEntity = reading.toEntity()
        val readingId = gpsReadingDao.insert(readingEntity)

        // Run anomaly detection
        val anomalies = anomalyDetector.detect(reading, previous)

        // Store any detected anomalies
        if (anomalies.isNotEmpty()) {
            val anomalyEntities = anomalies.map { it.toEntity(readingId) }
            anomalyDao.insertAll(anomalyEntities)
        }

        return anomalies
    }

    override fun getAllReadings(): Flow<List<GpsReadingEntity>> =
        gpsReadingDao.getAllReadings()

    override fun getReadingsBySession(sessionId: String): Flow<List<GpsReadingEntity>> =
        gpsReadingDao.getReadingsBySession(sessionId)

    override fun getAllAnomalies(): Flow<List<AnomalyEntity>> =
        anomalyDao.getAllAnomalies()

    override fun getAnomaliesBySession(sessionId: String): Flow<List<AnomalyEntity>> =
        anomalyDao.getAnomaliesBySession(sessionId)

    override fun getReadingCount(): Flow<Int> =
        gpsReadingDao.getReadingCount()

    override fun getAnomalyCount(): Flow<Int> =
        anomalyDao.getAnomalyCount()

    override fun getReadingCountForSession(sessionId: String): Flow<Int> =
        gpsReadingDao.getReadingCountForSession(sessionId)

    override fun getAnomalyCountForSession(sessionId: String): Flow<Int> =
        anomalyDao.getAnomalyCountForSession(sessionId)

    override fun getAllSessionIds(): Flow<List<String>> =
        gpsReadingDao.getAllSessionIds()

    override suspend fun getAllReadingsSync(): List<GpsReadingEntity> =
        gpsReadingDao.getAllReadingsSync()

    override suspend fun getAllAnomaliesSync(): List<AnomalyEntity> =
        anomalyDao.getAllAnomaliesSync()

    override suspend fun clearAllData() {
        anomalyDao.deleteAll()
        gpsReadingDao.deleteAll()
    }

    private fun GpsReading.toEntity() = GpsReadingEntity(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        timestamp = timestamp,
        isMock = isMock,
        provider = provider,
        satelliteCount = satelliteCount,
        sessionId = sessionId
    )

    private fun GpsReadingEntity.toDomain() = GpsReading(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        timestamp = timestamp,
        isMock = isMock,
        provider = provider,
        satelliteCount = satelliteCount,
        sessionId = sessionId
    )

    private fun Anomaly.toEntity(readingId: Long) = AnomalyEntity(
        readingId = readingId,
        type = type.name,
        severity = severity.name,
        description = description,
        value = value,
        threshold = threshold,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        sessionId = sessionId
    )
}
