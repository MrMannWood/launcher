package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import android.content.*
import android.content.pm.ResolveInfo
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.Result
import com.mrmannwood.hexlauncher.applist.AppData
import com.mrmannwood.hexlauncher.applist.AppListUpdater
import com.mrmannwood.hexlauncher.coroutine.LiveDataWithCoroutineScope
import com.mrmannwood.hexlauncher.icon.IconAdapter
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesLiveData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AppInfoLiveData private constructor(
        private val app: Application
): MediatorLiveData<Result<List<AppInfo>>>() {

    companion object {

        private lateinit var instance: AppInfoLiveData

        fun createAndGet(application: Application) : AppInfoLiveData {
            instance = AppInfoLiveData(application)
            return instance
        }

        fun get() : AppInfoLiveData {
            return instance
        }
    }

    init {
        PreferencesLiveData.get().observeForever(object : Observer<SharedPreferences> {
            override fun onChanged(prefs: SharedPreferences?) {
                if (prefs == null) return
                PreferencesLiveData.get().removeObserver(this)
                if (prefs.getBoolean(PreferenceKeys.Apps.USE_APP_DATABASE, false)) {
                    Timber.d("Using database for apps")
                    addSource(DatabaseLiveData(app)) { postValue(it) }
                } else {
                    Timber.d("Using pacman for apps")
                    addSource(PackageManagerLiveData(app)) { postValue(it) }
                }
            }
        })
    }

    private class DatabaseLiveData(context: Context): MediatorLiveData<Result<List<AppInfo>>>() {

        private val appContext = context.applicationContext

        init {
            addSource(DB.get().appDataDao().watchApps()) {
                onAppsLoaded(it)
            }
        }

        private fun onAppsLoaded(apps: List<AppData>) {
            try {
                postValue(Result.success(apps.map { loadApp(it) }))
            } catch (e: Exception) {
                Timber.e(e, "Error loading packages")
                postValue(Result.failure(e))
            }
        }

        private fun loadApp(appData: AppData) : AppInfo =
            AppInfo(
                packageName = appData.packageName,
                icon = IconAdapter.INSTANCE.makeIconDrawable(
                    appContext.resources, appData.foreground, appData.background),
                backgroundColor = appData.backgroundColor,
                label = appData.label
            )
    }

    private class PackageManagerLiveData(
        context: Context
    ): LiveDataWithCoroutineScope<Result<List<AppInfo>>>() {

        private val appContext = context.applicationContext

        private val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                performQuery()
            }
        }

        override fun onActive() {
            super.onActive()
            performQuery()
            LocalBroadcastManager.getInstance(appContext).registerReceiver(
                broadcastReceiver,
                IntentFilter(PackageObserverBroadcastReceiver.PACKAGES_CHANGED)
            )
        }

        override fun onInactive() {
            super.onInactive()
            LocalBroadcastManager.getInstance(appContext).unregisterReceiver(broadcastReceiver)
        }

        private fun performQuery() {
            scope?.launch {
                try {
                    postValue(Result.success(AppListUpdater.getAllInstalledApps(appContext) { it }
                        .map { loadApp(it) }))
                } catch (e: CancellationException) {
                    Timber.wtf(e, "Coroutines cancelled")
                    // don't post
                } catch (e: Exception) {
                    Timber.e(e, "Error loading packages")
                    postValue(Result.failure(e))
                }
            }
        }

        private suspend fun loadApp(pack: ResolveInfo) : AppInfo =
            withContext(Dispatchers.IO) {
                val icon = pack.loadIcon(appContext.packageManager)
                AppInfo(
                    packageName = pack.activityInfo.packageName,
                    icon = icon,
                    backgroundColor = IconAdapter.INSTANCE.getBackgroundColor(icon),
                    label = pack.loadLabel(appContext.packageManager).toString()
                )
            }

    }
}