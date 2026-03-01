package com.gpsjammingdetector.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.GnssStatus
import android.location.LocationManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.gpsjammingdetector.data.preferences.UserPreferences
import com.gpsjammingdetector.data.repository.GpsRepository
import com.gpsjammingdetector.domain.model.GpsReading
import com.gpsjammingdetector.util.AlertSoundPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class GpsTrackingService : Service() {

    @Inject lateinit var fusedLocationClient: FusedLocationProviderClient
    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var repository: GpsRepository
    @Inject lateinit var userPreferences: UserPreferences

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sessionId = ""
    private var readingCount = 0
    private var anomalyCount = 0
    private var satelliteCount = 0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                val reading = GpsReading(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    accuracy = location.accuracy,
                    speed = location.speed,
                    bearing = location.bearing,
                    timestamp = location.time,
                    isMock = location.isMock,
                    provider = location.provider ?: "unknown",
                    satelliteCount = satelliteCount,
                    sessionId = sessionId
                )

                serviceScope.launch {
                    try {
                        val anomalies = repository.processNewReading(reading)
                        readingCount++
                        anomalyCount += anomalies.size
                        _currentSatelliteCount.value = satelliteCount
                        _currentAccuracy.value = location.accuracy
                        _lastLatitude.value = location.latitude
                        _lastLongitude.value = location.longitude

                        if (anomalies.isNotEmpty()) {
                            userPreferences.audioAlertEnabled.collect { enabled ->
                                if (enabled) {
                                    AlertSoundPlayer.play(this@GpsTrackingService)
                                }
                                return@collect
                            }
                        }

                        updateNotification()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing reading", e)
                    }
                }
            }
        }
    }

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            satelliteCount = status.satelliteCount
        }
    }

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == NotificationHelper.ACTION_STOP) {
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        ContextCompat.registerReceiver(
            this, stopReceiver,
            IntentFilter(NotificationHelper.ACTION_STOP),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sessionId = UUID.randomUUID().toString()
        readingCount = 0
        anomalyCount = 0

        _isTracking.value = true
        _currentSessionId.value = sessionId

        val notification = NotificationHelper.buildNotification(this, 0, 0)
        startForeground(NotificationHelper.NOTIFICATION_ID, notification)

        val intervalMs = intent?.getLongExtra(EXTRA_INTERVAL_MS, 2000L) ?: 2000L

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        try {
            locationManager.registerGnssStatusCallback(gnssStatusCallback, android.os.Handler(Looper.getMainLooper()))
        } catch (e: Exception) {
            Log.w(TAG, "Could not register GNSS status callback", e)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        _isTracking.value = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        try {
            locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
        } catch (_: Exception) {}
        try {
            unregisterReceiver(stopReceiver)
        } catch (_: Exception) {}
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateNotification() {
        val notification = NotificationHelper.buildNotification(this, readingCount, anomalyCount)
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NotificationHelper.NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "GpsTrackingService"
        const val EXTRA_INTERVAL_MS = "interval_ms"

        private val _isTracking = MutableStateFlow(false)
        val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

        private val _currentSessionId = MutableStateFlow("")
        val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

        private val _currentSatelliteCount = MutableStateFlow(0)
        val currentSatelliteCount: StateFlow<Int> = _currentSatelliteCount.asStateFlow()

        private val _currentAccuracy = MutableStateFlow(0f)
        val currentAccuracy: StateFlow<Float> = _currentAccuracy.asStateFlow()

        private val _lastLatitude = MutableStateFlow(0.0)
        val lastLatitude: StateFlow<Double> = _lastLatitude.asStateFlow()

        private val _lastLongitude = MutableStateFlow(0.0)
        val lastLongitude: StateFlow<Double> = _lastLongitude.asStateFlow()

        fun start(context: Context, intervalMs: Long = 2000L) {
            val intent = Intent(context, GpsTrackingService::class.java).apply {
                putExtra(EXTRA_INTERVAL_MS, intervalMs)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, GpsTrackingService::class.java))
        }
    }
}
