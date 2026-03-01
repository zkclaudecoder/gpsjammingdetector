package com.gpsjammingdetector.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun PermissionHandler(
    hasLocationPermission: Boolean,
    hasBackgroundLocationPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onRequestBackgroundLocationPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    content: @Composable () -> Unit
) {
    if (!hasLocationPermission) {
        PermissionCard(
            title = "Location Permission Required",
            description = "This app needs access to your precise location to detect GPS anomalies.",
            buttonText = "Grant Location Access",
            onClick = onRequestLocationPermission
        )
    } else if (!hasBackgroundLocationPermission) {
        Column {
            PermissionCard(
                title = "Background Location",
                description = "For continuous monitoring, allow location access \"All the time\" in settings.",
                buttonText = "Enable Background Location",
                onClick = onRequestBackgroundLocationPermission
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    } else if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Column {
            PermissionCard(
                title = "Notification Permission",
                description = "Allow notifications to see tracking status.",
                buttonText = "Allow Notifications",
                onClick = onRequestNotificationPermission
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    } else {
        content()
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}
