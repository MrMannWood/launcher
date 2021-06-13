package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.mrmannwood.hexlauncher.DB

private var appInfoLiveData : LiveData<List<AppInfo>>? = null

fun getAppInfoLiveData(appContext: Application) : LiveData<List<AppInfo>> {
    if (appInfoLiveData == null) {
        appInfoLiveData = Transformations.map(DB.get().appDataDao().watchApps()) { apps ->
            apps.map { appData ->
                AppInfo(
                    packageName = appData.packageName,
                    icon = appContext.packageManager.getApplicationIcon(appData.packageName),
                    backgroundColor = appData.backgroundColor,
                    label = appData.label
                )
            }
        }
    }
    return appInfoLiveData!!
}
