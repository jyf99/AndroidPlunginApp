package com.jyf.androidplunginapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.util.Log
import java.io.IOException
import java.io.InputStream

object InstallApk {
    private const val PACKAGE_INSTALLED_ACTION = "com.android.apis.content.SESSION_API_PACKAGE_INSTALLED"

    @JvmStatic
    fun installApk(context: Context, path: String, cls: Class<*>) {
        var session: PackageInstaller.Session? = null
        try {
            val packageInstaller = context.packageManager.packageInstaller
            val params = SessionParams(
                SessionParams.MODE_FULL_INSTALL
            )
            val sessionId = packageInstaller.createSession(params)
            session = packageInstaller.openSession(sessionId)
            addApkToInstallSession(context.assets.open(path), session)

            // Create an install status receiver.
            val intent = Intent(context, cls)
            intent.action = PACKAGE_INSTALLED_ACTION
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            val statusReceiver = pendingIntent.intentSender

            // Commit the session (this will start the installation workflow).
            session.commit(statusReceiver)
        } catch (e: IOException) {
            Log.e(TAG, "InstallApk --- installApk, IOException. ${e.message}")
        } catch (e: RuntimeException) {
            session?.abandon()
            Log.e(TAG, "InstallApk --- installApk, RuntimeException. ${e.message}")
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    private fun addApkToInstallSession(inputStream: InputStream, session: PackageInstaller.Session) {
        // It's recommended to pass the file size to openWrite(). Otherwise installation may fail
        // if the disk is almost full.
        session.openWrite("package", 0, -1).use { packageInSession ->
            inputStream.use { input ->
                val buffer = ByteArray(16384)
                var n: Int
                while (input.read(buffer).also { n = it } >= 0) {
                    packageInSession.write(buffer, 0, n)
                }
            }
        }
    }

    /**
     * use it at Activity to call back,
     * this Activity must run in singleTop launchMode for it to be able to receive the intent
     * in onNewIntent().
     */
    @JvmStatic
    fun onNewIntent(context: Context, intent: Intent, callBack: CallBack? = null) {
        val extras = intent.extras
        if (PACKAGE_INSTALLED_ACTION == intent.action) {
            val status = extras?.getInt(PackageInstaller.EXTRA_STATUS)
            val message = extras?.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)
            when (status) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    // This test app isn't privileged, so the user has to confirm the install.
                    val confirmIntent = extras[Intent.EXTRA_INTENT] as Intent?
                    context.startActivity(confirmIntent)
                }
                PackageInstaller.STATUS_SUCCESS -> {
                    Log.d(TAG, "Install succeeded!")
                    callBack?.installSuccess()
                }
                PackageInstaller.STATUS_FAILURE,
                PackageInstaller.STATUS_FAILURE_ABORTED,
                PackageInstaller.STATUS_FAILURE_BLOCKED,
                PackageInstaller.STATUS_FAILURE_CONFLICT,
                PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
                PackageInstaller.STATUS_FAILURE_INVALID,
                PackageInstaller.STATUS_FAILURE_STORAGE -> {
                    Log.d(TAG, "Install failed! $status, $message")
                    callBack?.installFailed()
                }
                else -> {
                    Log.d(TAG, "Unrecognized status received from installer: $status")
                    callBack?.err()
                }
            }
        }
    }

    abstract class CallBack {
        open fun installSuccess() { }
        open fun installFailed() { }
        open fun err() { }
    }
}