package com.gpsjammingdetector.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.util.Log

enum class AlertSound(val displayName: String) {
    ALARM("Device Alarm"),
    NOTIFICATION("Notification"),
    RINGTONE("Ringtone"),
    BEEP_HIGH("High Beep"),
    BEEP_LOW("Low Beep"),
    SIREN("Siren Tone");
}

object AlertSoundPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null

    fun play(context: Context, sound: AlertSound = AlertSound.ALARM) {
        try {
            stop()
            when (sound) {
                AlertSound.ALARM -> playRingtoneType(context, RingtoneManager.TYPE_ALARM)
                AlertSound.NOTIFICATION -> playRingtoneType(context, RingtoneManager.TYPE_NOTIFICATION)
                AlertSound.RINGTONE -> playRingtoneType(context, RingtoneManager.TYPE_RINGTONE)
                AlertSound.BEEP_HIGH -> playTone(ToneGenerator.TONE_PROP_BEEP)
                AlertSound.BEEP_LOW -> playTone(ToneGenerator.TONE_PROP_BEEP2)
                AlertSound.SIREN -> playTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD)
            }
        } catch (e: Exception) {
            Log.e("AlertSoundPlayer", "Failed to play alert sound", e)
        }
    }

    private fun playRingtoneType(context: Context, type: Int) {
        val uri = RingtoneManager.getDefaultUri(type)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: return

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(context, uri)
            isLooping = false
            prepare()
            start()
            setOnCompletionListener { stop() }
        }
    }

    private fun playTone(toneType: Int) {
        toneGenerator = ToneGenerator(
            android.media.AudioManager.STREAM_ALARM,
            ToneGenerator.MAX_VOLUME
        ).apply {
            startTone(toneType, 2000)
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

        try {
            toneGenerator?.apply {
                stopTone()
                release()
            }
        } catch (_: Exception) {}
        toneGenerator = null
    }
}
