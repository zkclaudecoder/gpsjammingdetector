package com.gpsjammingdetector.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gpsjammingdetector.ui.theme.SeverityCritical

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val exportStatus by viewModel.exportStatus.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    val readingsExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let { viewModel.exportReadingsCsv(it) } }

    val anomaliesExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let { viewModel.exportAnomaliesCsv(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Detection Thresholds
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Detection Thresholds",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Max Speed
                ThresholdSlider(
                    label = "Max Speed",
                    value = config.maxSpeedKmh.toFloat(),
                    valueRange = 50f..1000f,
                    unit = "km/h",
                    onValueChange = { viewModel.updateMaxSpeed(it.toDouble()) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Teleportation Distance
                ThresholdSlider(
                    label = "Teleportation Distance",
                    value = config.teleportationDistanceMeters.toFloat(),
                    valueRange = 500f..50000f,
                    unit = "m",
                    onValueChange = { viewModel.updateTeleportationDistance(it.toDouble()) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Altitude Spike
                ThresholdSlider(
                    label = "Altitude Spike",
                    value = config.altitudeSpikeMeters.toFloat(),
                    valueRange = 50f..5000f,
                    unit = "m",
                    onValueChange = { viewModel.updateAltitudeSpike(it.toDouble()) }
                )
            }
        }

        // GPS Update Interval
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "GPS Update Interval",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                val intervalSeconds = config.gpsUpdateIntervalMs / 1000f
                ThresholdSlider(
                    label = "Interval",
                    value = intervalSeconds,
                    valueRange = 1f..30f,
                    unit = "sec",
                    onValueChange = { viewModel.updateGpsInterval((it * 1000).toLong()) }
                )
                Text(
                    "Note: Restart tracking for interval changes to take effect.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Data Export
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Data Export",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { readingsExportLauncher.launch("gps_readings.csv") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export Readings")
                    }
                    OutlinedButton(
                        onClick = { anomaliesExportLauncher.launch("anomalies.csv") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export Anomalies")
                    }
                }
            }
        }

        // Clear Data
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Data Management",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SeverityCritical)
                ) {
                    Text("Clear All Data")
                }
            }
        }

        // Status snackbar
        exportStatus?.let { status ->
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.dismissStatus() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(status)
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // bottom nav padding
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will permanently delete all GPS readings and detected anomalies. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = SeverityCritical)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ThresholdSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${"%.0f".format(value)} $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
