package com.gpsjammingdetector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gpsjammingdetector.data.local.entity.GpsReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsReadingDao {

    @Insert
    suspend fun insert(reading: GpsReadingEntity): Long

    @Query("SELECT * FROM gps_readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<GpsReadingEntity>>

    @Query("SELECT * FROM gps_readings WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getReadingsBySession(sessionId: String): Flow<List<GpsReadingEntity>>

    @Query("SELECT * FROM gps_readings ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestReading(): GpsReadingEntity?

    @Query("SELECT * FROM gps_readings WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestReadingForSession(sessionId: String): GpsReadingEntity?

    @Query("SELECT COUNT(*) FROM gps_readings")
    fun getReadingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM gps_readings WHERE sessionId = :sessionId")
    fun getReadingCountForSession(sessionId: String): Flow<Int>

    @Query("SELECT DISTINCT sessionId FROM gps_readings ORDER BY timestamp DESC")
    fun getAllSessionIds(): Flow<List<String>>

    @Query("SELECT * FROM gps_readings ORDER BY timestamp ASC")
    suspend fun getAllReadingsSync(): List<GpsReadingEntity>

    @Query("DELETE FROM gps_readings")
    suspend fun deleteAll()
}
