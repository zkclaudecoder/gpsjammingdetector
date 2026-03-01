package com.gpsjammingdetector.ui.screens.anomalies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpsjammingdetector.data.local.entity.AnomalyEntity
import com.gpsjammingdetector.data.repository.GpsRepository
import com.gpsjammingdetector.domain.model.AnomalyType
import com.gpsjammingdetector.domain.model.Severity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AnomaliesViewModel @Inject constructor(
    repository: GpsRepository
) : ViewModel() {

    private val _selectedTypes = MutableStateFlow<Set<AnomalyType>>(emptySet())
    val selectedTypes: StateFlow<Set<AnomalyType>> = _selectedTypes.asStateFlow()

    private val _selectedSeverities = MutableStateFlow<Set<Severity>>(emptySet())
    val selectedSeverities: StateFlow<Set<Severity>> = _selectedSeverities.asStateFlow()

    private val allAnomalies = repository.getAllAnomalies()

    val filteredAnomalies: StateFlow<List<AnomalyEntity>> = combine(
        allAnomalies,
        _selectedTypes,
        _selectedSeverities
    ) { anomalies, types, severities ->
        anomalies.filter { anomaly ->
            (types.isEmpty() || AnomalyType.valueOf(anomaly.type) in types) &&
                (severities.isEmpty() || Severity.valueOf(anomaly.severity) in severities)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleTypeFilter(type: AnomalyType) {
        _selectedTypes.value = _selectedTypes.value.let {
            if (type in it) it - type else it + type
        }
    }

    fun toggleSeverityFilter(severity: Severity) {
        _selectedSeverities.value = _selectedSeverities.value.let {
            if (severity in it) it - severity else it + severity
        }
    }
}
