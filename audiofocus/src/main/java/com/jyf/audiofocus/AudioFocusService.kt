package com.jyf.audiofocus

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi

class AudioFocusService : Service() {

    private var audioFocusManager: AudioFocusManage? = null

    private val binder = object : AudioFocusRemoteService.Stub() {

        @RequiresApi(Build.VERSION_CODES.O)
        override fun requestAudioFocusRequest(
            @NonNull listener: AudioFocusRemoteChangeListener,
            @Nullable focusGain: Int,
            @Nullable usage: Int,
            @Nullable contentType: Int,
            @Nullable acceptsDelayedFocusGain: Boolean
        ): Int {
            return audioFocusManager?.requestAudioFocus(
                listener, focusGain, usage, contentType, acceptsDelayedFocusGain)
                ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun abandonAudioFocusRequest(): Int {
            return audioFocusManager?.abandonAudioFocusRequest()
                ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
        }

        override fun requestAudioFocus(
            @NonNull listener: AudioFocusRemoteChangeListener,
            @Nullable streamType: Int,
            @Nullable durationHint: Int
        ): Int {
            return audioFocusManager?.requestAudioFocus(listener, streamType, durationHint)
                ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
        }

        override fun abandonAudioFocus(): Int {
            return audioFocusManager?.abandonAudioFocus()
                ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
        }
    }

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AudioFocusService ---- onCreate")

        audioFocusManager = AudioFocusManage(this)
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