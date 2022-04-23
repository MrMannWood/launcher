package com.mrmannwood.hexlauncher.iconpack

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.mrmannwood.hexlauncher.executors.PackageManagerExecutor
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.getAppInfoLiveData

class IconPackAppListLiveData(
    application: Application
) : MediatorLiveData<List<AppInfo>>() {

    private var appInfo: Map<String, AppInfo>? = null
    private var packages: List<String>? = null

    init {
        addSource(getAppInfoLiveData(application, true)) {
            appInfo = it.associateBy { it.componentName.packageName }
            combine()
        }
        addSource(IconPackLiveData(application)) {
            packages = it
            combine()
        }
    }

    private fun combine() {
        val appInfo = appInfo ?: return
        val packages = packages ?: return

        postValue(packages.mapNotNull { appInfo[it] }.toList())
    }

    private class IconPackLiveData(private val context: Context) : LiveData<List<String>>() {
        companion object {
            private val APP_ICON_ACTIONS = listOf(
                "com.gau.go.launcherex.theme",
                "org.adw.launcher.THEMES"
            )
        }

        override fun onActive() {
            super.onActive()
            PackageManagerExecutor.execute {
                postValue(
                    APP_ICON_ACTIONS
                        .flatMap { action ->
                            context.packageManager.queryIntentActivities(
                                Intent(action), PackageManager.GET_META_DATA
                            )
                        }
                        .map { it.activityInfo.packageName }
                        .distinct()
                )
            }
        }
    }
}
