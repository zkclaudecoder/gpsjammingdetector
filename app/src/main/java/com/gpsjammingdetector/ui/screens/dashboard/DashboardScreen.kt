package com.gpsjammingdetector.ui.screens.dashboard

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gpsjammingdetector.ui.components.PermissionHandler
import com.gpsjammingdetector.ui.theme.AllClear
import com.gpsjammingdetector.ui.theme.SeverityCritical
import com.gpsjammingdetector.util.AlertSoundPlayer

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val isTracking by viewModel.isTracking.collectAsStateWithLifecycle()
    val satelliteCount by viewModel.satelliteCount.collectAsStateWithLifecycle()
    val currentAccuracy by viewModel.currentAccuracy.collectAsStateWithLifecycle()
    val totalReadings by viewModel.totalReadingCount.collectAsStateWithLifecycle()
    val totalAnomalies by viewModel.totalAnomalyCount.collectAsStateWithLifecycle()
    val sessionReadings by viewModel.sessionReadingCount.collectAsStateWithLifecycle()
    val sessionAnomalies by viewModel.sessionAnomalyCount.collectAsStateWithLifecycle()
    val lastLat by viewModel.lastLatitude.collectAsStateWithLifecycle()
    val lastLon by viewModel.lastLongitude.collectAsStateWithLifecycle()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var hasBackgroundPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasBackgroundPermission = granted }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasNotificationPermission = granted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "GPS Anomaly Detector",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        PermissionHandler(
            hasLocationPermission = hasLocationPermission,
            hasBackgroundLocationPermission = hasBackgroundPermission,
            hasNotificationPermission = hasNotificationPermission,
            onRequestLocationPermission = {
                locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            onRequestBackgroundLocationPermission = {
                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            },
            onRequestNotificationPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        ) {
            // Status Banner
            StatusBanner(isTracking = isTracking, anomalyCount = sessionAnomalies)

            Spacer(modifier = Modifier.height(4.dp))

            // Start/Stop Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isTracking) viewModel.stopTracking() else viewModel.startTracking()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTracking) SeverityCritical else AllClear
                    )
                ) {
                    Icon(
                        imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isTracking) "  Stop Tracking" else "  Start Tracking",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Button(
                    onClick = { AlertSoundPlayer.stop() },
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeOff,
                        contentDescription = "Stop alert sound",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Session Stats
            if (isTracking) {
                Text("Current Session", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.GpsFixed,
                        label = "Readings",
                        value = sessionReadings.toString()
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Warning,
                        label = "Anomalies",
                        value = sessionAnomalies.toString()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Satellite,
                        label = "Satellites",
                        value = satelliteCount.toString()
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        label = "Accuracy",
                        value = if (currentAccuracy > 0) "%.1fm".format(currentAccuracy) else "--"
                    )
                }

                // Last Coordinates
                if (lastLat != 0.0 || lastLon != 0.0) {
                    val coordText = "%.6f, %.6f".format(lastLat, lastLon)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    "Last Position",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    coordText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("coordinates", coordText))
                                Toast.makeText(context, "Coordinates copied", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy coordinates"
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total Stats
            Text("All Time", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.GpsFixed,
                    label = "Total Readings",
                    value = totalReadings.toString()
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Warning,
                    label = "Total Anomalies",
                    value = totalAnomalies.toString()
                )
            }
        }
    }
}

@Composable
private fun StatusBanner(isTracking: Boolean, anomalyCount: Int) {
    val backgroundColor = when {
        !isTracking -> MaterialTheme.colorScheme.surfaceVariant
        anomalyCount > 0 -> SeverityCritical.copy(alpha = 0.1f)
        else -> AllClear.copy(alpha = 0.1f)
    }
    val statusText = when {
        !isTracking -> "Not Tracking"
        anomalyCount > 0 -> "Anomalies Detected!"
        else -> "All Clear"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isTracking && anomalyCount == 0) Icons.Default.GpsFixed else Icons.Default.Warning,
                contentDescription = null,
                tint = when {
                    !isTracking -> MaterialTheme.colorScheme.onSurfaceVariant
                    anomalyCount > 0 -> SeverityCritical
                    else -> AllClear
                }
            )
            Text(
                text = "  $statusText",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    !isTracking -> MaterialTheme.colorScheme.onSurfaceVariant
                    anomalyCount > 0 -> SeverityCritical
                    else -> AllClear
                }
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
