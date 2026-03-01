package com.gpsjammingdetector.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpsjammingdetector.data.local.entity.AnomalyEntity
import com.gpsjammingdetector.data.local.entity.GpsReadingEntity
import com.gpsjammingdetector.data.repository.GpsRepository
import com.gpsjammingdetector.service.GpsTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: GpsRepository
) : ViewModel() {

    private val _selectedSessionId = MutableStateFlow("")
    val selectedSessionId: StateFlow<String> = _selectedSessionId.asStateFlow()

    val sessionIds = repository.getAllSessionIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @Suppress("OPT_IN_USAGE")
    val readings: StateFlow<List<GpsReadingEntity>> = _selectedSessionId
        .flatMapLatest { sessionId ->
            if (sessionId.isNotEmpty()) repository.getReadingsBySession(sessionId)
            else repository.getAllReadings()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @Suppress("OPT_IN_USAGE")
    val anomalies: StateFlow<List<AnomalyEntity>> = _selectedSessionId
        .flatMapLatest { sessionId ->
            if (sessionId.isNotEmpty()) repository.getAnomaliesBySession(sessionId)
            else repository.getAllAnomalies()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val autoFollow = MutableStateFlow(false)

    init {
        // Default to current session if tracking
        val currentSession = GpsTrackingService.currentSessionId.value
        if (currentSession.isNotEmpty()) {
            _selectedSessionId.value = currentSession
        }
    }

    fun selectSession(sessionId: String) {
        _selectedSessionId.value = sessionId
    }

    fun toggleAutoFollow() {
        autoFollow.value = !autoFollow.value
    }
}
