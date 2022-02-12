package com.mrmannwood.hexlauncher

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import com.mrmannwood.hexlauncher.applist.AppListUpdater
import com.mrmannwood.hexlauncher.applist.writeAppsToFile
import com.mrmannwood.hexlauncher.foregrounddetection.ForegroundActivityListener
import com.mrmannwood.hexlauncher.launcher.PackageObserverBroadcastReceiver
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository
import com.mrmannwood.hexlauncher.timber.FileLoggerTree
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.launcher.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.io.File
import java.util.ArrayList
import java.util.concurrent.CountDownLatch

class LauncherApplication : Application() {

    companion object {
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(FileLoggerTree.getAndInit(this@LauncherApplication))
        if (BuildConfig.DEBUG) {
            DebugBuildModeConfiguration.onApplicationCreate(this@LauncherApplication)
        } else {
            ReleaseBuildModeConfiguration.onApplicationCreate(this@LauncherApplication)
        }
        installUncaughtExceptionHandler()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        PreferencesRepository.watchPref(
            context = this@LauncherApplication,
            key = PreferenceKeys.Logging.ENABLE_DISK_LOGGING,
            extractor = PreferenceExtractor.BooleanExtractor
        )
            .onEach { enable ->
                if (enable == true) {
                    FileLoggerTree.get().enableDiskFlush()
                } else {
                    FileLoggerTree.get().disableDiskFlush()
                }
            }.launchIn(applicationScope)

        DB.init(this@LauncherApplication)

        applicationScope.launch {
            AppListUpdater.updateAppList(applicationContext)
        }

        registerReceiver(
                PackageObserverBroadcastReceiver(),
                IntentFilter().apply {
                    addDataScheme("package")
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                }
        )

        ForegroundActivityListener.init(this)
        CoroutineScope(Dispatchers.IO).launch  {
            File(filesDir, "rage_shake").deleteRecursively()
        }
    }

    private fun installUncaughtExceptionHandler() {
        val exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Timber.e(throwable, "An uncaught exception occurred on thread ${thread.name}")
            } catch (ignored: Throwable) { }
            exceptionHandler?.uncaughtException(thread, throwable)
        }
    }

    fun rageShakeThing(activity: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            val rageShakeDir = File(filesDir, "rage_shake")
            writeAppsToFile(rageShakeDir)

            val latch = CountDownLatch(1)

            FileLoggerTree.get().copyLogsTo(File(filesDir, "rage_shake")) { latch.countDown() }

            runCatching { latch.await() }

            val uris = ArrayList(
                rageShakeDir.listFiles()?.map {
                    FileProvider.getUriForFile(
                        activity, "com.mrmannwood.hexlauncher.fileprovider", it)
                } ?: emptyList()
            )

            val debugInfo ="""
                OS Version: ${System.getProperty("os.version")} (${Build.VERSION.INCREMENTAL})
                OS API Level: ${Build.VERSION.SDK_INT}
                Device: ${Build.DEVICE}
                Model and Product: ${Build.MODEL} ${Build.PRODUCT}
                App Version: ${getString(R.string.app_version)}
            """.trimIndent()

            activity.startActivity(
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.dev_email)))
                    putExtra(Intent.EXTRA_SUBJECT, "Rage Shake Report")
                    putExtra(Intent.EXTRA_STREAM, uris)
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.rage_shake_email_body, debugInfo))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            )
        }
    }

    private interface BuildModeConfiguration {
        fun onApplicationCreate(application: Application)
    }

    private object DebugBuildModeConfiguration : BuildModeConfiguration {
        override fun onApplicationCreate(application: Application) {
            Timber.plant(Timber.DebugTree())

            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().build())
        }
    }

    private object ReleaseBuildModeConfiguration : BuildModeConfiguration {
        override fun onApplicationCreate(application: Application) {
        }
    }
}