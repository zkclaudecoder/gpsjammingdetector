package com.gpsjammingdetector.di

import android.content.Context
import androidx.room.Room
import com.gpsjammingdetector.data.local.AppDatabase
import com.gpsjammingdetector.data.local.dao.AnomalyDao
import com.gpsjammingdetector.data.local.dao.GpsReadingDao
import com.gpsjammingdetector.data.repository.GpsRepository
import com.gpsjammingdetector.data.repository.GpsRepositoryImpl
import com.gpsjammingdetector.domain.detection.AnomalyDetector
import com.gpsjammingdetector.domain.model.DetectionConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "gps_detector.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideGpsReadingDao(db: AppDatabase): GpsReadingDao = db.gpsReadingDao()

    @Provides
    fun provideAnomalyDao(db: AppDatabase): AnomalyDao = db.anomalyDao()

    @Provides
    @Singleton
    fun provideDetectionConfig(): DetectionConfig = DetectionConfig()

    @Provides
    @Singleton
    fun provideAnomalyDetector(config: DetectionConfig): AnomalyDetector =
        AnomalyDetector(config)

    @Provides
    @Singleton
    fun provideGpsRepository(
        gpsReadingDao: GpsReadingDao,
        anomalyDao: AnomalyDao,
        anomalyDetector: AnomalyDetector
    ): GpsRepository = GpsRepositoryImpl(gpsReadingDao, anomalyDao, anomalyDetector)
}
