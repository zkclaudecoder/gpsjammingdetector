package com.gpsjammingdetector.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.gpsjammingdetector.R
import com.gpsjammingdetector.ui.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "gps_tracking_channel"
    const val NOTIFICATION_ID = 1
    const val ACTION_STOP = "com.gpsjammingdetector.STOP_TRACKING"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "GPS Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows GPS tracking status"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun buildNotification(
        context: Context,
        readingCount: Int,
        anomalyCount: Int
    ): Notification {
        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(ACTION_STOP).setPackage(context.packageName),
            PendingIntent.FLAG_IMMUTABLE
        )

        val text = "Readings: $readingCount | Anomalies: $anomalyCount"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("GPS Tracking Active")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openIntent)
            .addAction(R.drawable.ic_notification, "Stop", stopIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }
}
