package com.mrmannwood.hexlauncher

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.StrictMode
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.mrmannwood.hexlauncher.applist.AppListUpdater
import com.mrmannwood.hexlauncher.foregrounddetection.ForegroundActivityListener
import com.mrmannwood.hexlauncher.launcher.AppInfoLiveData
import com.mrmannwood.hexlauncher.launcher.PackageObserverBroadcastReceiver
import com.mrmannwood.hexlauncher.rageshake.ShakeManager
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferencesLiveData
import com.mrmannwood.hexlauncher.timber.FileLoggerTree
import com.mrmannwood.launcher.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        PreferencesLiveData.create(this).observeForever {
            PreferenceLiveData(
                PreferenceKeys.Logging.ENABLE_DISK_LOGGING,
                PreferenceLiveData.Extractor.BooleanExtractor
            ).observeForever { enable ->
                if (enable == true) {
                    FileLoggerTree.get().enableDiskFlush()
                } else {
                    FileLoggerTree.get().disableDiskFlush()
                }
            }
        }

        DB.init(this@LauncherApplication)

        applicationScope.launch {
            AppListUpdater.updateAppList(applicationContext)
        }

        AppInfoLiveData.createAndGet(this).observeForever { }

        registerReceiver(
                PackageObserverBroadcastReceiver(),
                IntentFilter().apply {
                    addDataScheme("package")
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                }
        )

        // TODO make rageshake do something useful
        if (BuildConfig.DEBUG) {
            ForegroundActivityListener.init(this)
            val shakeManager = ShakeManager(3) {
                ForegroundActivityListener.forCurrentForegroundActivity { activity ->
                    Toast.makeText(activity, "Rage Shake", Toast.LENGTH_LONG).show()
                }
            }
            ForegroundActivityListener.registerForegroundUpdateListener { inForeground ->
                if (inForeground) {
                    shakeManager.startRageShakeDetector(this@LauncherApplication)
                } else {
                    shakeManager.stopRageShakeDetector()
                }
            }
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