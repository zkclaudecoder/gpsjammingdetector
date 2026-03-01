package com.gpsjammingdetector.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log

object AlertSoundPlayer {

    private var mediaPlayer: MediaPlayer? = null

    fun play(context: Context) {
        try {
            stop()
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: return

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, alarmUri)
                isLooping = false
                prepare()
                start()
                setOnCompletionListener { stop() }
            }
        } catch (e: Exception) {
            Log.e("AlertSoundPlayer", "Failed to play alert sound", e)
        }
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }
}
