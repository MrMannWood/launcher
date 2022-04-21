package com.mrmannwood.hexlauncher.applist

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.annotation.WorkerThread
import com.mrmannwood.applist.AppListManager
import com.mrmannwood.applist.LauncherItem
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.executors.diskExecutor
import com.mrmannwood.hexlauncher.icon.IconAdapter
import timber.log.Timber

object AppListUpdater {

    fun updateAppList(context: Context, appListManager: AppListManager) {
        diskExecutor.execute {
            updateAppListOnWorkerThread(context.applicationContext, appListManager)
        }
    }

    @WorkerThread
    fun updateAppListOnWorkerThread(context: Context, appListManager: AppListManager) {
        try {
            val installedApps = appListManager.queryAppList()
            val appDao = DB.get(context).appDataDao()

            appDao.deleteNotIncluded(installedApps.map { it.componentName })

            val appUpdateTimes = appDao.getLastUpdateTimeStamps().associateBy({ it.componentName }, { it.timestamp })
            installedApps
                .map { it to appUpdateTimes.getOrElse(it.componentName) { -1L } }
                .filter { (launcherItem, lastUpdateTime) -> launcherItem.lastUpdateTime > lastUpdateTime }
                .map { convertToAppData(it.first) }
                .forEach {
                    try {
                        if (it.lastUpdateTime == -1L) {
                            Timber.d("Inserting ${it.componentName.flattenToString()}")
                            appDao.insert(it)
                        } else {
                            Timber.d("Updating ${it.componentName.flattenToString()}")
                            appDao.update(it.label, it.lastUpdateTime, it.backgroundColor, it.componentName)
                        }
                    } catch (e: SQLiteException) {
                        Timber.e(e, "An error occurred while writing app to db: $it")
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "An error occurred while updating the app database")
        }
    }

    @WorkerThread
    private fun convertToAppData(launcherItem: LauncherItem) : AppData {
        return AppData(
            componentName = launcherItem.componentName,
            label = launcherItem.label,
            lastUpdateTime = launcherItem.lastUpdateTime,
            backgroundColor = IconAdapter.INSTANCE.getBackgroundColor(launcherItem.icon),
            tags = emptyList()
        )
    }
}
