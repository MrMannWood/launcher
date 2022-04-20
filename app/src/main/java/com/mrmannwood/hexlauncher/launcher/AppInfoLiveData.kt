package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Color
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.LauncherApplication
import com.mrmannwood.hexlauncher.executors.cpuBoundTaskExecutor
import com.mrmannwood.hexlauncher.livedata.combineWith
import com.mrmannwood.launcher.R

private var appInfoLiveData : LiveData<List<AppInfo>>? = null
private val categoryMap: MutableMap<Int, List<String>> = HashMap()

fun getSingleAppLiveData(context: Context, componentName: ComponentName) : LiveData<AppInfo?> {
    return Transformations.map(makeLiveData(context.applicationContext as Application, true)) {
        it.firstOrNull{ it.componentName == componentName }
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
    return (appContext as LauncherApplication).appListLiveData.combineWith(
        DB.get(appContext).appDataDao().watchApps(),
        cpuBoundTaskExecutor
    ) { appList, decorationList ->
        if (appList == null || decorationList == null) return@combineWith null
        val decorationMap = decorationList.associateBy { it.componentName }
        appList.map { it to decorationMap[it.componentName] }
            .filter { showHidden || it.second?.hidden == false }
            .map { (launcherItem, decoration) ->
                AppInfo(
                    launcherItem = launcherItem,
                    backgroundColor = decoration?.bgcOverride ?: decoration?.backgroundColor ?: Color.TRANSPARENT,
                    hidden = decoration?.hidden == true,
                    backgroundHidden = decoration?.backgroundHidden == true,
                    categories = getCategories(appContext, launcherItem.category),
                    tags = decoration?.tags ?: emptyList()
                )
            }.toList()
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