package com.example.testapp

import android.app.Application
import com.example.testapp.contacts.ContactsLoader
import com.example.testapp.launcher.AppInfoLiveData
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

        ContactsLoader.loadContacts(this, "bridger") { result ->
            for (name in result) {
                Timber.d("Marshall: $name")
            }
        }
    }
}