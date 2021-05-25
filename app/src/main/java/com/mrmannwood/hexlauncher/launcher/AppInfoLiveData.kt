package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MediatorLiveData
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.Result
import com.mrmannwood.hexlauncher.applist.AppData
import com.mrmannwood.hexlauncher.icon.IconAdapter
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

    private val handler = Handler(Looper.getMainLooper())

    init {
        addSource(DB.get().appDataDao().watchApps()) {
            onAppsLoaded(it)
        }
    }

    private fun onAppsLoaded(apps: List<AppData>) {
        try {
            val previous = value
            postValue(Result.success(apps.map { loadApp(it) }))
            if (previous != null) {
                handler.postDelayed(
                    {
                        previous.onSuccess { apps ->
                            apps.forEach { app ->
                                IconAdapter.INSTANCE.closeIconDrawable(app.icon)
                            }
                        }
                    },
                    5
                )
            }
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