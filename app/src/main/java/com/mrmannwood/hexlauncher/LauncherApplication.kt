package com.mrmannwood.hexlauncher

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.StrictMode
import com.mrmannwood.hexlauncher.Result
import com.mrmannwood.hexlauncher.applist.AppListUpdater
import com.mrmannwood.hexlauncher.launcher.AppInfoLiveData
import com.mrmannwood.hexlauncher.launcher.PackageObserverBroadcastReceiver
import com.mrmannwood.hexlauncher.settings.PreferencesLiveData
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
        if (BuildConfig.DEBUG) {
            DebugBuildModeConfiguration.onApplicationCreate()
        } else {
            ReleaseBuildModeConfiguration.onApplicationCreate()
        }

        PreferencesLiveData.create(this).observeForever { }

        DB.init(this@LauncherApplication)

        applicationScope.launch {
            AppListUpdater.updateAppListInternal(applicationContext)
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