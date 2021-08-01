package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.applist.AppData
import com.mrmannwood.hexlauncher.applist.DecoratedAppData
import timber.log.Timber
import java.util.*

private var appInfoLiveData : LiveData<List<AppInfo>>? = null

fun getSingleAppLiveData(context: Context, packageName: String) : LiveData<AppInfo?> {
    return Transformations.map(DB.get().appDataDao().watchApp(packageName)) {
        transformAppInfo(context, it)
    }
}

fun getAppInfoLiveData(appContext: Application, showHidden: Boolean = false) : LiveData<List<AppInfo>> {
    if (showHidden) {
        return makeLiveData(appContext, true)
    }
    if (appInfoLiveData == null) {
        appInfoLiveData = makeLiveData(appContext, false)
    }
    return appInfoLiveData!!
}

private fun makeLiveData(appContext: Application, showHidden: Boolean = false) : LiveData<List<AppInfo>> {
    return Transformations.map(DB.get().appDataDao().watchApps()) { apps ->
        apps.filter {
            if (showHidden) {
                true
            } else {
                !it.decoration.hidden
            }
        }.mapNotNull { transformAppInfo(appContext, it) }
    }
}

private fun transformAppInfo(context: Context, app: DecoratedAppData) : AppInfo? {
    return try {
        AppInfo(
            packageName = app.appData.packageName,
            icon = context.packageManager.getApplicationIcon(app.appData.packageName),
            backgroundColor = app.decoration.bgcOverride ?: app.appData.backgroundColor,
            label = app.appData.label,
            hidden = app.decoration.hidden,
            backgroundHidden = app.decoration.backgroundHidden
        )
    } catch (e: PackageManager.NameNotFoundException) {
        Timber.w(e, "Package manager error while loading apps")
        null
    }
}