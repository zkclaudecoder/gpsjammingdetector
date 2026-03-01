package com.gpsjammingdetector.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gpsjammingdetector.data.local.dao.AnomalyDao
import com.gpsjammingdetector.data.local.dao.GpsReadingDao
import com.gpsjammingdetector.data.local.entity.AnomalyEntity
import com.gpsjammingdetector.data.local.entity.GpsReadingEntity

@Database(
    entities = [GpsReadingEntity::class, AnomalyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gpsReadingDao(): GpsReadingDao
    abstract fun anomalyDao(): AnomalyDao
}
