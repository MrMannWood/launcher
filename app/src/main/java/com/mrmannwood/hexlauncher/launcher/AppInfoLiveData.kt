package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.Result
import com.mrmannwood.hexlauncher.applist.AppData
import timber.log.Timber

class AppInfoLiveData private constructor(
        private val appContext: Application
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
            icon = appContext.packageManager.getApplicationIcon(appData.packageName),
            backgroundColor = appData.backgroundColor,
            label = appData.label
        )
}