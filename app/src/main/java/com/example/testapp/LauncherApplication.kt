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

        ContactsLoader.tryCreate(this)?.loadContacts("br") { result ->
            Timber.d("Marshall: start contacts print")
            for (contact in result) {
                Timber.d("Marshall: $contact")
            }
            Timber.d("Marshall: end contacts print")
        }
    }
}