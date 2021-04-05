package com.mrmannwood.hexlauncher

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.StrictMode
import com.mrmannwood.hexlauncher.launcher.AppInfoLiveData
import com.mrmannwood.hexlauncher.launcher.PackageObserverBroadcastReceiver
import com.mrmannwood.hexlauncher.settings.PreferencesLiveData
import com.mrmannwood.launcher.BuildConfig
import timber.log.Timber

class LauncherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            DebugBuildModeConfiguration.onApplicationCreate()
        } else {
            ReleaseBuildModeConfiguration.onApplicationCreate()
        }

        PreferencesLiveData.create(this).observeForever { }

        AppInfoLiveData.createAndGet(this).observeForever { result ->
            result.onSuccess { apps ->
                Timber.i("App info changed, got ${apps.size} apps")
            }
            result.onFailure { error ->
                Timber.e(error, "App Info changed, got error")
            }
        }

        registerReceiver(
                PackageObserverBroadcastReceiver(),
                IntentFilter().apply {
                    addDataScheme("package")
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                }
        )
    }

    private interface BuildModeConfiguration {
        fun onApplicationCreate()
    }

    private object DebugBuildModeConfiguration : BuildModeConfiguration {
        override fun onApplicationCreate() {
            Timber.plant(Timber.DebugTree())

            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().build())
        }
    }

    private object ReleaseBuildModeConfiguration : BuildModeConfiguration {
        override fun onApplicationCreate() { }
    }
}