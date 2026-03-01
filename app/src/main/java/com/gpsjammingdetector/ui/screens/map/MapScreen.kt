package com.gpsjammingdetector.ui.screens.map

import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
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

    val context = LocalContext.current

    // Configure osmdroid
    DisposableEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
        }
        onDispose { }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(3.0)
        }
    }

    // Update map overlays when data changes
    LaunchedEffect(readings, anomalies) {
        mapView.overlays.clear()

        val sortedReadings = readings.sortedBy { it.timestamp }

        // Draw GPS track polyline
        if (sortedReadings.size >= 2) {
            val polyline = Polyline().apply {
                getOutlinePaint().color = AndroidColor.parseColor("#4CAF50")
                getOutlinePaint().strokeWidth = 8f
                setPoints(sortedReadings.map { GeoPoint(it.latitude, it.longitude) })
            }
            mapView.overlays.add(polyline)
        }

        // Draw anomaly markers
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        anomalies.forEach { anomaly ->
            val markerColor = when (anomaly.severity) {
                "CRITICAL" -> AndroidColor.parseColor("#D32F2F")
                "HIGH" -> AndroidColor.parseColor("#FF5722")
                "MEDIUM" -> AndroidColor.parseColor("#FF9800")
                else -> AndroidColor.parseColor("#FFC107")
            }

            val marker = Marker(mapView).apply {
                position = GeoPoint(anomaly.latitude, anomaly.longitude)
                title = "${anomaly.type}: ${anomaly.severity}"
                snippet = "${anomaly.description} at ${dateFormat.format(Date(anomaly.timestamp))}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                // Create colored circle drawable as marker icon
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setSize(40, 40)
                    setColor(markerColor)
                    setStroke(3, AndroidColor.WHITE)
                }
                icon = drawable
            }
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    // Auto-follow camera
    LaunchedEffect(readings, autoFollow) {
        if (autoFollow && readings.isNotEmpty()) {
            val sortedReadings = readings.sortedBy { it.timestamp }
            val last = sortedReadings.last()
            mapView.controller.animateTo(GeoPoint(last.latitude, last.longitude), 16.0, 500L)
        }
    }

    // Lifecycle
    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

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
