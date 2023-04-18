package com.mrmannwood.hexlauncher.applist

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.annotation.WorkerThread
import com.mrmannwood.applist.LauncherItem
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.executors.diskExecutor
import com.mrmannwood.hexlauncher.icon.IconAdapter
import timber.log.Timber

object AppListUpdater {

    fun updateAppList(context: Context, installedApps: List<LauncherItem>) {
        diskExecutor.execute {
            updateAppListOnWorkerThread(context.applicationContext, installedApps)
        }
    }

    @WorkerThread
    fun updateAppListOnWorkerThread(context: Context, installedApps: List<LauncherItem>) {
        try {
            val appDao = DB.get(context).appDataDao()

            appDao.deleteNotIncluded(installedApps.map { it.componentName })

            val appUpdateTimes =
                appDao.getLastUpdateTimeStamps().associateBy({ it.componentName }, { it.timestamp })
            installedApps
                .map { it to appUpdateTimes.getOrElse(it.componentName) { -1L } }
                .onEach { Timber.d("${it.first.componentName.flattenToString()} -> ${it.second}") }
                .filter { (launcherItem, lastUpdateTime) -> lastUpdateTime == -1L || launcherItem.lastUpdateTime > lastUpdateTime }
                .map { convertToAppData(it.first) to it.second }
                .forEach { (appData, lastUpdateTime) ->
                    try {
                        if (lastUpdateTime == -1L) {
                            Timber.d("Inserting ${appData.componentName.flattenToString()}")
                            appDao.insert(appData)
                        } else {
                            Timber.d("Updating ${appData.componentName.flattenToString()}")
                            appDao.update(
                                appData.label,
                                appData.lastUpdateTime,
                                appData.backgroundColor,
                                appData.componentName
                            )
                        }
                    } catch (e: SQLiteException) {
                        Timber.e(e, "An error occurred while writing app to db: $appData")
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "An error occurred while updating the app database")
        }
    }

    @WorkerThread
    private fun convertToAppData(launcherItem: LauncherItem): AppData {
        return AppData(
            componentName = launcherItem.componentName,
            label = launcherItem.label,
            lastUpdateTime = launcherItem.lastUpdateTime,
            backgroundColor = IconAdapter.INSTANCE.getBackgroundColor(launcherItem.icon),
            tags = emptyList()
        )
    }
}
