package com.mrmannwood.hexlauncher

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.hexlauncher.contacts.ContactsLoader
import com.mrmannwood.hexlauncher.launcher.AppInfoLiveData
import com.mrmannwood.hexlauncher.launcher.PackageObserverBroadcastReceiver
import timber.log.Timber

class LauncherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        AppInfoLiveData.get(this).observeForever { result ->
            result.onSuccess { apps ->
                Timber.i("App info changed, got ${apps.size} aps")
            }
            result.onFailure { error ->
                Timber.e(error, "App Info changed, got error")
            }
        }

        ContactsLoader.tryCreate(this)?.loadContacts("br") { result ->
            for (contact in result) {
                Timber.d("$contact")
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
}