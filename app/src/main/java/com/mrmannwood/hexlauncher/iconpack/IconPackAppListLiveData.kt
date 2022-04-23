package com.mrmannwood.hexlauncher.iconpack

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import com.mrmannwood.hexlauncher.executors.PackageManagerExecutor
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.getAppInfoLiveData
import com.mrmannwood.iconpack.IconPackAppLiveData

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
        addSource(IconPackAppLiveData(application, PackageManagerExecutor)) {
            packages = it
            combine()
        }
    }

    private fun combine() {
        val appInfo = appInfo ?: return
        val packages = packages ?: return

        postValue(packages.mapNotNull { appInfo[it] }.toList())
    }
}
