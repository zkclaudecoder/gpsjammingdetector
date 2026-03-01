package com.gpsjammingdetector.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gpsjammingdetector.data.preferences.UserPreferences
import com.gpsjammingdetector.data.repository.GpsRepository
import com.gpsjammingdetector.service.GpsTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val application: Application,
    private val repository: GpsRepository,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    val isTracking: StateFlow<Boolean> = GpsTrackingService.isTracking
    val currentSessionId: StateFlow<String> = GpsTrackingService.currentSessionId
    val satelliteCount: StateFlow<Int> = GpsTrackingService.currentSatelliteCount
    val currentAccuracy: StateFlow<Float> = GpsTrackingService.currentAccuracy
    val lastLatitude: StateFlow<Double> = GpsTrackingService.lastLatitude
    val lastLongitude: StateFlow<Double> = GpsTrackingService.lastLongitude

    val totalReadingCount = repository.getReadingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalAnomalyCount = repository.getAnomalyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @Suppress("OPT_IN_USAGE")
    val sessionReadingCount = GpsTrackingService.currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId.isNotEmpty()) repository.getReadingCountForSession(sessionId)
            else flowOf(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @Suppress("OPT_IN_USAGE")
    val sessionAnomalyCount = GpsTrackingService.currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId.isNotEmpty()) repository.getAnomalyCountForSession(sessionId)
            else flowOf(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun startTracking() {
        viewModelScope.launch {
            userPreferences.detectionConfig.collect { config ->
                GpsTrackingService.start(application, config.gpsUpdateIntervalMs)
                return@collect
            }
        }
    }

    fun stopTracking() {
        GpsTrackingService.stop(application)
    }
}
