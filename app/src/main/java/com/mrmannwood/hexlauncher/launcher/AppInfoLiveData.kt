package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.applist.DecoratedAppData
import com.mrmannwood.hexlauncher.executors.PackageManagerExecutor
import com.mrmannwood.launcher.R
import timber.log.Timber

private var appInfoLiveData : LiveData<List<AppInfo>>? = null
private val categoryMap: MutableMap<Int, List<String>> = HashMap()

fun getSingleAppLiveData(context: Context, packageName: String) : LiveData<AppInfo?> {
    return Transformations.map(DB.get(context).appDataDao().watchApp(packageName)) {
        transformAppInfo(context.applicationContext, it)
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
    return Transformations.map(DB.get(appContext).appDataDao().watchApps()) { apps ->
        apps.filter {
            if (showHidden) {
                true
            } else {
                !it.decoration.hidden
            }
        }.mapNotNull { transformAppInfo(appContext, it) }
    }
}

@MainThread
private fun transformAppInfo(context: Context, app: DecoratedAppData) : AppInfo? {
    return try {
        AppInfo(
            packageName = app.appData.packageName,
            icon = Provider({ context.packageManager.getApplicationIcon(app.appData.packageName) }, PackageManagerExecutor),
            backgroundColor = app.decoration.bgcOverride ?: app.appData.backgroundColor,
            label = app.appData.label,
            hidden = app.decoration.hidden,
            backgroundHidden = app.decoration.backgroundHidden,
            categories = getCategories(context, app.appData.category),
            tags = app.decoration.tags
        )
    } catch (e: PackageManager.NameNotFoundException) {
        Timber.w(e, "Package manager error while loading apps")
        null
    }
}

@MainThread
private fun getCategories(context: Context, category: Int): List<String> {
    return categoryMap.getOrPut(category) {
        context.resources.getStringArray(
            when (category) {
                ApplicationInfo.CATEGORY_ACCESSIBILITY -> R.array.APP_CATEGORY_ACCESSIBILITY
                ApplicationInfo.CATEGORY_AUDIO -> R.array.APP_CATEGORY_AUDIO
                ApplicationInfo.CATEGORY_GAME -> R.array.APP_CATEGORY_GAME
                ApplicationInfo.CATEGORY_IMAGE -> R.array.APP_CATEGORY_IMAGE
                ApplicationInfo.CATEGORY_MAPS -> R.array.APP_CATEGORY_MAPS
                ApplicationInfo.CATEGORY_NEWS -> R.array.APP_CATEGORY_NEWS
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> R.array.APP_CATEGORY_PRODUCTIVITY
                ApplicationInfo.CATEGORY_SOCIAL -> R.array.APP_CATEGORY_SOCIAL
                ApplicationInfo.CATEGORY_VIDEO -> R.array.APP_CATEGORY_VIDEO
                else -> R.array.APP_CATEGORY_UNDEFINED
            }
        ).toList()
    }
}