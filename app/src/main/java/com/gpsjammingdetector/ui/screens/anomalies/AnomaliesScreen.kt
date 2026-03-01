package com.gpsjammingdetector.ui.screens.anomalies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gpsjammingdetector.data.local.entity.AnomalyEntity
import com.gpsjammingdetector.domain.model.AnomalyType
import com.gpsjammingdetector.domain.model.Severity
import com.gpsjammingdetector.ui.theme.SeverityCritical
import com.gpsjammingdetector.ui.theme.SeverityHigh
import com.gpsjammingdetector.ui.theme.SeverityLow
import com.gpsjammingdetector.ui.theme.SeverityMedium
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnomaliesScreen(
    viewModel: AnomaliesViewModel = hiltViewModel(),
    onNavigateToMap: (Double, Double) -> Unit = { _, _ -> }
) {
    val anomalies by viewModel.filteredAnomalies.collectAsStateWithLifecycle()
    val selectedTypes by viewModel.selectedTypes.collectAsStateWithLifecycle()
    val selectedSeverities by viewModel.selectedSeverities.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Anomalies",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Type filters
        Text("Filter by Type", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AnomalyType.entries.forEach { type ->
                FilterChip(
                    selected = type in selectedTypes,
                    onClick = { viewModel.toggleTypeFilter(type) },
                    label = { Text(type.displayName, style = MaterialTheme.typography.bodySmall) }
                )
            }
        }

        // Severity filters
        Text("Filter by Severity", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Severity.entries.forEach { severity ->
                FilterChip(
                    selected = severity in selectedSeverities,
                    onClick = { viewModel.toggleSeverityFilter(severity) },
                    label = { Text(severity.name, style = MaterialTheme.typography.bodySmall) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (anomalies.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No anomalies detected yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Text(
                "${anomalies.size} anomalies",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                items(anomalies, key = { it.id }) { anomaly ->
                    AnomalyCard(
                        anomaly = anomaly,
                        onClick = { onNavigateToMap(anomaly.latitude, anomaly.longitude) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnomalyCard(anomaly: AnomalyEntity, onClick: () -> Unit) {
    val severityColor = when (anomaly.severity) {
        "CRITICAL" -> SeverityCritical
        "HIGH" -> SeverityHigh
        "MEDIUM" -> SeverityMedium
        "LOW" -> SeverityLow
        else -> Color.Gray
    }

    val icon = anomalyIcon(anomaly.type)
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = severityColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Severity color band
            Card(
                colors = CardDefaults.cardColors(containerColor = severityColor),
                modifier = Modifier.size(40.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AnomalyType.valueOf(anomaly.type).displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = anomaly.severity,
                        style = MaterialTheme.typography.labelSmall,
                        color = severityColor
                    )
                }
                Text(
                    text = anomaly.description,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${dateFormat.format(Date(anomaly.timestamp))} | ${
                        "%.4f".format(anomaly.latitude)
                    }, ${"%.4f".format(anomaly.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun anomalyIcon(type: String): ImageVector = when (type) {
    "TELEPORTATION" -> Icons.Default.Flight
    "IMPOSSIBLE_SPEED" -> Icons.Default.Speed
    "ALTITUDE_SPIKE" -> Icons.Default.Height
    "MOCK_LOCATION" -> Icons.Default.GpsOff
    else -> Icons.Default.GpsOff
}
