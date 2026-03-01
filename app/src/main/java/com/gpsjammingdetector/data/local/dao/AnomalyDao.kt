package com.gpsjammingdetector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gpsjammingdetector.data.local.entity.AnomalyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnomalyDao {

    @Insert
    suspend fun insert(anomaly: AnomalyEntity)

    @Insert
    suspend fun insertAll(anomalies: List<AnomalyEntity>)

    @Query("SELECT * FROM anomalies ORDER BY timestamp DESC")
    fun getAllAnomalies(): Flow<List<AnomalyEntity>>

    @Query("SELECT * FROM anomalies WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getAnomaliesBySession(sessionId: String): Flow<List<AnomalyEntity>>

    @Query("SELECT COUNT(*) FROM anomalies")
    fun getAnomalyCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM anomalies WHERE sessionId = :sessionId")
    fun getAnomalyCountForSession(sessionId: String): Flow<Int>

    @Query("SELECT * FROM anomalies ORDER BY timestamp ASC")
    suspend fun getAllAnomaliesSync(): List<AnomalyEntity>

    @Query("DELETE FROM anomalies")
    suspend fun deleteAll()
}
