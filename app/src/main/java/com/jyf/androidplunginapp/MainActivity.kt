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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.jyf.androidplunginapp.databinding.ActivityMainBinding
import com.jyf.audiofocus.AudioFocusRemoteChangeListener
import com.jyf.audiofocus.AudioFocusRemoteService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        bindAudioFocusService()
    }

    private fun bindAudioFocusService() {
        val isBind = {
            bindService(Intent().apply {
                action = AudioFocusRemoteService::class.java.name
                component = ComponentName("com.jyf.audiofocus", "com.jyf.audiofocus.AudioFocusService")
            }, connection, BIND_AUTO_CREATE)
        }
        if (!isBind()) {
            val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                lifecycleScope.launch {
                    while (!isBind()) {
                        delay(500)
                    }
                }
            }
            launcher.launch(Intent().apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                component = ComponentName("com.jyf.audiofocus", "com.jyf.audiofocus.MainActivity")
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}