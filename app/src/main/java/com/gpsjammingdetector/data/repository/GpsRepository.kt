package com.gpsjammingdetector.data.repository

import com.gpsjammingdetector.data.local.entity.AnomalyEntity
import com.gpsjammingdetector.data.local.entity.GpsReadingEntity
import com.gpsjammingdetector.domain.model.Anomaly
import com.gpsjammingdetector.domain.model.GpsReading
import kotlinx.coroutines.flow.Flow

interface GpsRepository {
    suspend fun processNewReading(reading: GpsReading): List<Anomaly>
    fun getAllReadings(): Flow<List<GpsReadingEntity>>
    fun getReadingsBySession(sessionId: String): Flow<List<GpsReadingEntity>>
    fun getAllAnomalies(): Flow<List<AnomalyEntity>>
    fun getAnomaliesBySession(sessionId: String): Flow<List<AnomalyEntity>>
    fun getReadingCount(): Flow<Int>
    fun getAnomalyCount(): Flow<Int>
    fun getReadingCountForSession(sessionId: String): Flow<Int>
    fun getAnomalyCountForSession(sessionId: String): Flow<Int>
    fun getAllSessionIds(): Flow<List<String>>
    suspend fun getAllReadingsSync(): List<GpsReadingEntity>
    suspend fun getAllAnomaliesSync(): List<AnomalyEntity>
    suspend fun clearAllData()
}
