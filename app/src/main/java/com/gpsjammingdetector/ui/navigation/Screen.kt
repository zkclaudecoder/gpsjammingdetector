package com.gpsjammingdetector.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Map : Screen("map", "Map", Icons.Default.Map)
    data object Anomalies : Screen("anomalies", "Anomalies", Icons.AutoMirrored.Filled.List)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val items = listOf(Dashboard, Map, Anomalies, Settings)
    }
}
