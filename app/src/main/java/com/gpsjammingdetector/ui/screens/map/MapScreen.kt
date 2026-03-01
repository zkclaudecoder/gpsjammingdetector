package com.gpsjammingdetector.ui.screens.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.gpsjammingdetector.ui.theme.AllClear
import com.gpsjammingdetector.ui.theme.SeverityCritical
import com.gpsjammingdetector.ui.theme.SeverityHigh
import com.gpsjammingdetector.ui.theme.SeverityLow
import com.gpsjammingdetector.ui.theme.SeverityMedium
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val readings by viewModel.readings.collectAsStateWithLifecycle()
    val anomalies by viewModel.anomalies.collectAsStateWithLifecycle()
    val sessionIds by viewModel.sessionIds.collectAsStateWithLifecycle()
    val selectedSessionId by viewModel.selectedSessionId.collectAsStateWithLifecycle()
    val autoFollow by viewModel.autoFollow.collectAsStateWithLifecycle()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    // Auto-follow camera to latest position
    LaunchedEffect(readings, autoFollow) {
        if (autoFollow && readings.isNotEmpty()) {
            val latest = readings.first() // readings are ordered DESC for "all", ASC for session
            val sortedReadings = readings.sortedBy { it.timestamp }
            val last = sortedReadings.last()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(last.latitude, last.longitude), 16f
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL
            )
        ) {
            // Draw GPS track polyline
            val sortedReadings = readings.sortedBy { it.timestamp }
            if (sortedReadings.size >= 2) {
                val points = sortedReadings.map { LatLng(it.latitude, it.longitude) }
                Polyline(
                    points = points,
                    color = AllClear,
                    width = 8f
                )
            }

            // Draw anomaly markers
            val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            anomalies.forEach { anomaly ->
                val hue = when (anomaly.severity) {
                    "CRITICAL" -> BitmapDescriptorFactory.HUE_RED
                    "HIGH" -> BitmapDescriptorFactory.HUE_ORANGE
                    "MEDIUM" -> BitmapDescriptorFactory.HUE_YELLOW
                    else -> BitmapDescriptorFactory.HUE_GREEN
                }
                Marker(
                    state = MarkerState(position = LatLng(anomaly.latitude, anomaly.longitude)),
                    title = "${anomaly.type}: ${anomaly.severity}",
                    snippet = "${anomaly.description} at ${dateFormat.format(Date(anomaly.timestamp))}",
                    icon = BitmapDescriptorFactory.defaultMarker(hue)
                )
            }
        }

        // Session picker dropdown
        if (sessionIds.isNotEmpty()) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
                    .fillMaxWidth(0.9f)
            ) {
                TextField(
                    value = if (selectedSessionId.isEmpty()) "All Sessions"
                    else "Session ${selectedSessionId.take(8)}...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Sessions") },
                        onClick = {
                            viewModel.selectSession("")
                            expanded = false
                        }
                    )
                    sessionIds.forEach { sessionId ->
                        DropdownMenuItem(
                            text = { Text("Session ${sessionId.take(8)}...") },
                            onClick = {
                                viewModel.selectSession(sessionId)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Auto-follow FAB
        FloatingActionButton(
            onClick = { viewModel.toggleAutoFollow() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = if (autoFollow) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = if (autoFollow) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                contentDescription = "Toggle auto-follow"
            )
        }
    }
}
