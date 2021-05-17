package com.mrmannwood.hexlauncher.applist

import android.content.Context
import android.content.Intent
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

                val lastCheckTime = prefs.getLong(LAST_APP_CHECK_KEY, 0)

                val cachedApps = DB.get().appDataDao().getApps().associateBy { it.packageName }

                val appsFromSystem = getAppsFromSystem(appContext, appContext.packageManager).associateBy { it.second }
                val appsToDelete = cachedApps
                    .filter { app -> !appsFromSystem.containsKey(app.value.packageName) }
                    .map { app -> app.value.packageName }

                val toUpdate = appsFromSystem
                    .map { it.value }
                    .filter { app -> app.first >= lastCheckTime }
                    .map {
                        val icon = it.third.loadIcon(appContext.packageManager)
                        AppData(
                            packageName = it.second,
                            label = it.third.loadLabel(appContext.packageManager).toString(),
                            lastUpdateTime = it.first,
                            backgroundColor = IconAdapter.INSTANCE.getBackgroundColor(icon)
                                ?: 0xFFC1CC,
                            foreground = IconAdapter.INSTANCE.getForegroundBitmap(icon),
                            background = IconAdapter.INSTANCE.getBackgroundBitmap(icon)
                        )
                    }

                if (appsToDelete.isNotEmpty()) {
                    Timber.d("Deleting ${appsToDelete.size} apps")
                    DB.get().appDataDao().deleteAll(appsToDelete)
                }

                if (toUpdate.isNotEmpty()) {
                    Timber.d("Insert/updating ${appsToDelete.size} apps")
                    DB.get().appDataDao().insertAll(toUpdate)
                    toUpdate.forEach {
                        Timber.d("Inserted ${it.label}: ${it.background != null}")
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
    private fun getAppsFromSystem(context: Context, pacman: PackageManager) : List<Triple<Long, String, ResolveInfo>> {
        return pacman.queryIntentActivities(
            Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0
        )
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                resolveInfo to pacman.getPackageInfo(resolveInfo.activityInfo.packageName, 0)
            }.map { (resolveInfo, packageInfo) ->
                Triple(
                    packageInfo.lastUpdateTime,
                    resolveInfo.activityInfo.packageName,
                    resolveInfo
                )
            }
    }
}
