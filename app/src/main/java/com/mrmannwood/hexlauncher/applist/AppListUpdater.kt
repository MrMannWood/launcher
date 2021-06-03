package com.mrmannwood.hexlauncher.applist

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import androidx.annotation.WorkerThread
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.icon.IconAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber

object AppListUpdater {

    suspend fun updateAppList(context: Context) {
        updateAppListWithCount(context, 0)
    }

    private suspend fun updateAppListWithCount(context: Context, count: Int) {
        val appContext = context.applicationContext
        withContext(Dispatchers.IO) {
            var runAgain = false
            try {
                val installedApps = getInstalledApps(appContext)
                val appDao = DB.get().appDataDao()

                appDao.deleteNotIncluded(installedApps)

                val appUpdateTimes = appDao.getLastUpdateTimeStamps().associateBy({ it.packageName }, {it.timestamp})
                for (packageName in installedApps) {
                    val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
                    val lastUpdateTime = appUpdateTimes.getOrElse(packageName, { -1L })
                    if (lastUpdateTime >= packageInfo.lastUpdateTime) continue
                    Timber.d("Inserting $packageName")

                    loadAppDataFromPacman(packageInfo, context.packageManager)?.use { appData ->
                        try {
                            appDao.insert(appData)
                        } catch (e: SQLiteException) {
                            Timber.e(e, "An error occurred while writing app to db: $appData")
                        }
                    } ?: run {
                        runAgain = true
                        Timber.d("$packageName had a null icon")
                    }
                }

            } catch (e: Exception) {
                Timber.e(e, "An error occurred while updating the app database")
            }
            if (runAgain && count <= 5) {
                delay(100)
                updateAppListWithCount(appContext, count + 1)
            }
        }
    }

    @WorkerThread
    private fun getInstalledApps(context: Context) : List<String> {
        return context.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0
        )
            .filter { it.activityInfo.packageName != context.packageName }
            .map { it.activityInfo.packageName }
            .distinct() // apparently there are times where this is necessary
    }

    @WorkerThread
    private fun loadAppDataFromPacman(packageInfo: PackageInfo, pacman: PackageManager) : AppData? {
        val packageName = packageInfo.packageName
        val appInfo = packageInfo.applicationInfo
        val icon = appInfo.loadIcon(pacman)
        return if (IconAdapter.INSTANCE.isRecycled(icon)) {
            null
        } else {
            AppData(
                packageName = packageName,
                label = appInfo.loadLabel(pacman).toString(),
                lastUpdateTime = packageInfo.lastUpdateTime,
                backgroundColor = IconAdapter.INSTANCE.getBackgroundColor(icon),
                foreground = IconAdapter.INSTANCE.getForegroundBitmap(icon),
                background = IconAdapter.INSTANCE.getBackgroundBitmap(icon)
            )
        }
    }
}
