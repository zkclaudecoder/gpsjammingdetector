package com.gpsjammingdetector.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gpsjammingdetector.ui.navigation.AppNavigation
import com.gpsjammingdetector.ui.theme.GpsJammingDetectorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GpsJammingDetectorTheme {
                AppNavigation()
            }
        }
    }
}
