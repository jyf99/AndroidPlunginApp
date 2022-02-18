package com.jyf.audiofocus

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.util.Log

class AudioFocusService : Service() {

    private var audioManager: AudioManager? = null

    private val binder = object : AudioFocusRemoteService.Stub() {
        override fun requestAudioFocus() {

        }

        override fun abandonAudioFocus() {

        }

    }

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AudioFocusService ---- onCreate")

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager?
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "AudioFocusService ---- onStartCommand")
        notifyForeground()
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun notifyForeground() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = packageName
            val channel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager?)?.apply {
                if (getNotificationChannel(channelId) == null) {
                    createNotificationChannel(channel)
                }
            }
            Notification.Builder(this, channelId)
        } else {
            Notification.Builder(this)
        }

        startForeground(1, builder.setContentText("Audio service is running.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent).build())
    }
}