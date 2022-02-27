package com.jyf.androidplunginapp

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
            Log.d(TAG, "type = $focusChange")
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

        //requestPermission()
        bindAudioFocusService()
    }

    private fun requestPermission() {
        val permissions = arrayOf(
            Manifest.permission.INSTALL_PACKAGES,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        var granted = true
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                granted = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    }.launch(
                        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName"))
                    )
                } else {
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    }.launch(
                        Intent(Settings.ACTION_SECURITY_SETTINGS)
                    )
                }
                return
            }
        }
        if (granted) {
            InstallApk.installApk(this, "release/audfocus.apk", MainActivity::class.java)
            return
        }
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            granted = true
            result.values.forEach {
                if (!it) {
                    granted = it
                    return@forEach
                }
            }
            if (granted) {
                InstallApk.installApk(this, "release/audfocus.apk", MainActivity::class.java)
            } else {
                Toast.makeText(this, "must allow permission.", Toast.LENGTH_SHORT).show()
            }
        }

        requestPermissionLauncher.launch(permissions)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            InstallApk.onNewIntent(this, intent, object : InstallApk.CallBack() {
                override fun installSuccess() {
                    bindAudioFocusService()
                }
            })
        }
    }

    private fun bindAudioFocusService() {
        val isBind = {
            bindService(Intent().apply {
                action = AudioFocusRemoteService::class.java.name
                component = ComponentName("com.jyf.audiofocus", "com.jyf.audiofocus.AudioFocusService")
            }, connection, BIND_AUTO_CREATE)
        }
        if (!isBind()) {
            val startActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                lifecycleScope.launch {
                    while (!isBind()) {
                        delay(500)
                    }
                }
            }
            startActivityLauncher.launch(Intent().apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                component = ComponentName("com.jyf.audiofocus", "com.jyf.audiofocus.MainActivity")
            })
        }
    }
}