package com.mrmannwood.hexlauncher.applist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.icon.IconAdapter
import com.mrmannwood.hexlauncher.settings.PreferencesLiveData
import com.mrmannwood.launcher.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

object AppListUpdater {

    private const val LAST_APP_CHECK_KEY = "last_app_check_time"

    suspend fun updateAppListInternal(context: Context) {
        val appContext = context.applicationContext
        withContext(Dispatchers.IO) {
            try {
                val prefs = PreferencesLiveData.get().getSharedPreferences();

                val cachedApps = getCachedApps()
                val installedApps = getAllInstalledApps(appContext, appContext.packageManager)

                val deletedApps = findDeletedApps(cachedApps, installedApps)
                val updatedApps = findNewOrUpdatedApps(
                    appContext, getLastCacheUpdateTime(prefs), installedApps)

                if (deletedApps.isNotEmpty()) {
                    DB.get().appDataDao().deleteAll(deletedApps)
                }

                if (updatedApps.isNotEmpty()) {
                    DB.get().appDataDao().insertAll(updatedApps)
                    updatedApps.forEach {
                        it.foreground?.recycle()
                        it.background.recycle()
                    }
                }

                prefs.edit { putLong(LAST_APP_CHECK_KEY, System.currentTimeMillis()) }
            } catch (e: Exception) {
                Timber.e(e, "An error occurred while updating the app database")
                Toast.makeText(appContext, R.string.error_app_load, Toast.LENGTH_LONG).show()
            }
        }
    }

    @WorkerThread
    private fun getLastCacheUpdateTime(prefs: SharedPreferences) : Long =
        prefs.getLong(LAST_APP_CHECK_KEY, 0)

    @WorkerThread
    private fun getCachedApps() : Map<String, AppData> =
        DB.get()
            .appDataDao()
            .getApps()
            .associateBy { it.packageName }

    private fun findDeletedApps(
        cachedApps: Map<String, AppData>,
        installedApps: Map<String, Pair<Long, ResolveInfo>>
    ) : List<String> = cachedApps
        .filter { app -> !installedApps.containsKey(app.value.packageName) }
        .map { app -> app.value.packageName }

    private fun findNewOrUpdatedApps(
        context: Context,
        lastCacheUpdateTime: Long,
        installedApps: Map<String, Pair<Long, ResolveInfo>>
    ) : List<AppData> = installedApps
        .filter { app -> app.value.first >= lastCacheUpdateTime }
        .map {
            val packageName = it.key
            val lastUpdateTime = it.value.first
            val resolveInfo = it.value.second
            val icon = it.value.second.loadIcon(context.packageManager)
            AppData(
                packageName = packageName,
                label = resolveInfo.loadLabel(context.packageManager).toString(),
                lastUpdateTime = lastUpdateTime,
                backgroundColor = IconAdapter.INSTANCE.getBackgroundColor(icon)
                    ?: 0xFFC1CC,
                foreground = IconAdapter.INSTANCE.getForegroundBitmap(icon),
                background = IconAdapter.INSTANCE.getBackgroundBitmap(icon)
            )
        }

    @WorkerThread
    private fun getAllInstalledApps(context: Context, pacman: PackageManager) : Map<String, Pair<Long, ResolveInfo>> {
        return pacman.queryIntentActivities(
            Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0
        )
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                resolveInfo to pacman.getPackageInfo(resolveInfo.activityInfo.packageName, 0)
            }.map { (resolveInfo, packageInfo) ->
                Pair(
                    packageInfo.lastUpdateTime,
                    resolveInfo
                )
            }.associateBy { it.second.activityInfo.packageName }
    }
}
