package com.jyf.androidplunginapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.jyf.androidplunginapp.databinding.ActivityMainBinding
import com.jyf.audiofocus.AudioFocusRemoteChangeListener
import com.jyf.audiofocus.AudioFocusRemoteService

const val TAG = "AudioFocusTest"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val audioFocusChangeListener = object : AudioFocusRemoteChangeListener.Stub() {

        override fun onAudioFocusChange(focusChange: Int) {
            Log.e(TAG, "type = $focusChange")
        }
    }

    private var audioService: AudioFocusRemoteService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            audioService = AudioFocusRemoteService.Stub.asInterface(service)
            Log.d(TAG, "connected. $audioService")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "disconnected. $audioService")
            audioService?.abandonAudioFocusRequest()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindService(Intent().apply {
            action = AudioFocusRemoteService::class.java.name
            component = ComponentName("com.jyf.audiofocus", "com.jyf.audiofocus.AudioFocusService")
        }, connection, Context.BIND_AUTO_CREATE)

        binding.button.setOnClickListener {
            audioService?.requestAudioFocusRequest(
                audioFocusChangeListener,
                AudioManager.AUDIOFOCUS_GAIN,
                AudioAttributes.USAGE_MEDIA,
                AudioAttributes.CONTENT_TYPE_MUSIC,
                true
            )
        }

        binding.button2.setOnClickListener {
            audioService?.abandonAudioFocusRequest()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}