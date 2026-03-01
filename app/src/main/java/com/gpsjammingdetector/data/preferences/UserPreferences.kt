package com.gpsjammingdetector.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gpsjammingdetector.domain.model.DetectionConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val maxSpeedKey = doublePreferencesKey("max_speed_kmh")
    private val teleportationDistanceKey = doublePreferencesKey("teleportation_distance_m")
    private val altitudeSpikeKey = doublePreferencesKey("altitude_spike_m")
    private val gpsIntervalKey = longPreferencesKey("gps_interval_ms")

    val detectionConfig: Flow<DetectionConfig> = context.dataStore.data.map { prefs ->
        DetectionConfig(
            maxSpeedKmh = prefs[maxSpeedKey] ?: 300.0,
            teleportationDistanceMeters = prefs[teleportationDistanceKey] ?: 5000.0,
            altitudeSpikeMeters = prefs[altitudeSpikeKey] ?: 500.0,
            gpsUpdateIntervalMs = prefs[gpsIntervalKey] ?: 2000L
        )
    }

    suspend fun updateConfig(config: DetectionConfig) {
        context.dataStore.edit { prefs ->
            prefs[maxSpeedKey] = config.maxSpeedKmh
            prefs[teleportationDistanceKey] = config.teleportationDistanceMeters
            prefs[altitudeSpikeKey] = config.altitudeSpikeMeters
            prefs[gpsIntervalKey] = config.gpsUpdateIntervalMs
        }
    }
}
