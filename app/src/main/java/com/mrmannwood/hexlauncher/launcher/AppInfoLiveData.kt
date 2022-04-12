package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Color
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.LauncherApplication
import com.mrmannwood.hexlauncher.executors.cpuBoundTaskExecutor
import com.mrmannwood.hexlauncher.executors.diskExecutor
import com.mrmannwood.hexlauncher.executors.mainThreadExecutor
import com.mrmannwood.hexlauncher.livedata.combineWith
import com.mrmannwood.launcher.R
import java.util.concurrent.CountDownLatch

private var appInfoLiveData: LiveData<List<AppInfo>>? = null
private val categoryMap: MutableMap<Int, List<String>> = HashMap()

@AnyThread
fun getAppInfoForApps(context: Context, apps: List<String>, callback: (List<AppInfo>) -> Unit) {
    val appContext = context.applicationContext
    diskExecutor.execute {
        val appInfo = Array<AppInfo?>(apps.size) { null }
        val latch = CountDownLatch(appInfo.size)
        DB.get(context).appDataDao().getApps(apps).forEachIndexed { idx, appData ->
            mainThreadExecutor.execute {
                appInfo[idx] = transformAppInfo(appContext, appData)
                latch.countDown()
            }
        }
        latch.await()
        callback(appInfo.mapNotNull { it }.toList())
    }
}

fun getSingleAppLiveData(context: Context, componentName: ComponentName): LiveData<AppInfo?> {
    return Transformations.map(makeLiveData(context.applicationContext as Application, true)) {
        it.firstOrNull { it.componentName == componentName }
    }
}

fun getAppInfoLiveData(context: Context, showHidden: Boolean = false) : LiveData<List<AppInfo>> {
    val appContext = context.applicationContext
    if (showHidden) {
        return makeLiveData(appContext, true)
    }
    if (appInfoLiveData == null) {
        appInfoLiveData = makeLiveData(appContext, false)
    }
    return appInfoLiveData!!
}

private fun makeLiveData(appContext: Context, showHidden: Boolean = false): LiveData<List<AppInfo>> {
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
