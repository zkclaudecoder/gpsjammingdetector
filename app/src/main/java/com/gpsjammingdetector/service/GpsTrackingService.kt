package com.gpsjammingdetector.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
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

    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var repository: GpsRepository
    @Inject lateinit var userPreferences: UserPreferences

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sessionId = ""
    private var readingCount = 0
    private var anomalyCount = 0
    private var satelliteCount = 0

    private val gpsLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val reading = GpsReading(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                accuracy = location.accuracy,
                speed = location.speed,
                bearing = location.bearing,
                timestamp = location.time,
                isMock = location.isMock,
                provider = location.provider ?: LocationManager.GPS_PROVIDER,
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
                                userPreferences.alertSound.collect { sound ->
                                    AlertSoundPlayer.play(this@GpsTrackingService, sound)
                                    return@collect
                                }
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

        override fun onProviderEnabled(provider: String) {
            Log.i(TAG, "GPS provider enabled")
        }

        override fun onProviderDisabled(provider: String) {
            Log.w(TAG, "GPS provider disabled")
        }
    }

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            var usedCount = 0
            for (i in 0 until status.satelliteCount) {
                if (status.usedInFix(i)) usedCount++
            }
            satelliteCount = usedCount
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

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            intervalMs,
            0f,
            gpsLocationListener,
            Looper.getMainLooper()
        )

        try {
            locationManager.registerGnssStatusCallback(
                gnssStatusCallback,
                android.os.Handler(Looper.getMainLooper())
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not register GNSS status callback", e)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        _isTracking.value = false
        locationManager.removeUpdates(gpsLocationListener)
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
