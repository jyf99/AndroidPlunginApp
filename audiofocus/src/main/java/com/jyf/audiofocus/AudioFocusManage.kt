package com.jyf.audiofocus

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

internal class AudioFocusManage(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var focusRemoteChangeListener: AudioFocusRemoteChangeListener? = null
    private var focusChangeListener: AudioManager.OnAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener {
            Log.d(TAG, "AudioFocusManage --- OnAudioFocusChangeListener, type = $it")
            focusRemoteChangeListener?.onAudioFocusChange(it)
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private var focusRequest: AudioFocusRequest? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun requestAudioFocus(
        listener: AudioFocusRemoteChangeListener,
        focusGain: Int?,
        usage: Int?,
        contentType: Int?,
        acceptsDelayedFocusGain: Boolean?
    ): Int {
        focusRemoteChangeListener = listener
        focusRequest = AudioFocusRequest.Builder(focusGain ?:AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(usage ?: AudioAttributes.USAGE_MEDIA)
                    .setContentType(contentType ?: AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAcceptsDelayedFocusGain(acceptsDelayedFocusGain ?: true)
            .setOnAudioFocusChangeListener(focusChangeListener)
            .build()
        return focusRequest?.let { audioManager.requestAudioFocus(it) }
            ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun abandonAudioFocusRequest(): Int {
        return focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
    }

    fun requestAudioFocus(
        listener: AudioFocusRemoteChangeListener,
        streamType: Int?, durationHint: Int?
    ): Int {
        focusRemoteChangeListener = listener
        return audioManager.requestAudioFocus(
            focusChangeListener,
            streamType ?: AudioManager.STREAM_MUSIC,
            durationHint ?: AudioManager.AUDIOFOCUS_GAIN
        )
    }

    fun abandonAudioFocus(): Int {
        return audioManager.abandonAudioFocus(focusChangeListener)
    }
}