package com.gpsjammingdetector.ui.screens.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gpsjammingdetector.data.preferences.UserPreferences
import com.gpsjammingdetector.data.repository.GpsRepository
import com.gpsjammingdetector.domain.model.DetectionConfig
import com.gpsjammingdetector.util.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val repository: GpsRepository,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    val config: StateFlow<DetectionConfig> = userPreferences.detectionConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetectionConfig())

    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus.asStateFlow()

    fun updateMaxSpeed(value: Double) {
        viewModelScope.launch {
            userPreferences.updateConfig(config.value.copy(maxSpeedKmh = value))
        }
    }

    fun updateTeleportationDistance(value: Double) {
        viewModelScope.launch {
            userPreferences.updateConfig(config.value.copy(teleportationDistanceMeters = value))
        }
    }

    fun updateAltitudeSpike(value: Double) {
        viewModelScope.launch {
            userPreferences.updateConfig(config.value.copy(altitudeSpikeMeters = value))
        }
    }

    fun updateGpsInterval(valueMs: Long) {
        viewModelScope.launch {
            userPreferences.updateConfig(config.value.copy(gpsUpdateIntervalMs = valueMs))
        }
    }

    fun exportReadingsCsv(uri: Uri) {
        viewModelScope.launch {
            try {
                val readings = repository.getAllReadingsSync()
                application.contentResolver.openOutputStream(uri)?.use { stream ->
                    CsvExporter.exportReadings(readings, stream)
                }
                _exportStatus.value = "Exported ${readings.size} readings"
            } catch (e: Exception) {
                _exportStatus.value = "Export failed: ${e.message}"
            }
        }
    }

    fun exportAnomaliesCsv(uri: Uri) {
        viewModelScope.launch {
            try {
                val anomalies = repository.getAllAnomaliesSync()
                application.contentResolver.openOutputStream(uri)?.use { stream ->
                    CsvExporter.exportAnomalies(anomalies, stream)
                }
                _exportStatus.value = "Exported ${anomalies.size} anomalies"
            } catch (e: Exception) {
                _exportStatus.value = "Export failed: ${e.message}"
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _exportStatus.value = "All data cleared"
        }
    }

    fun dismissStatus() {
        _exportStatus.value = null
    }
}
