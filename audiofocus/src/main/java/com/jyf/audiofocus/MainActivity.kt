package com.jyf.audiofocus

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import java.lang.Exception

const val TAG = "RemoteAudioFocus"

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, AudioFocusService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "start service exception. ${e.message}")
        }
    }

    override fun onResume() {
        finish()
        super.onResume()
    }
}