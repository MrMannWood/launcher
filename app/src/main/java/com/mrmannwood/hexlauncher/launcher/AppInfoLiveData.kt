package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mrmannwood.hexlauncher.Result
import com.mrmannwood.hexlauncher.coroutine.LiveDataWithCoroutineScope
import com.mrmannwood.hexlauncher.icon.IconAdapter
import kotlinx.coroutines.*
import timber.log.Timber

class AppInfoLiveData private constructor(
        private val app: Application
): LiveDataWithCoroutineScope<Result<List<AppInfo>>>() {

    companion object {

        private lateinit var instance: AppInfoLiveData

        fun createAndGet(application: Application) : AppInfoLiveData {
            instance = AppInfoLiveData(application)
            return instance
        }

        fun get() : AppInfoLiveData {
            return instance
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            triggerLoad()
        }
    }

    override fun onActive() {
        super.onActive()
        triggerLoad()

        LocalBroadcastManager.getInstance(app).registerReceiver(
            broadcastReceiver,
            IntentFilter(PackageObserverBroadcastReceiver.PACKAGES_CHANGED)
        )
    }

    override fun onInactive() {
        super.onInactive()
        LocalBroadcastManager.getInstance(app).unregisterReceiver(broadcastReceiver)
    }

    private fun getPackageManager() : PackageManager = app.packageManager

    private fun triggerLoad() {
        scope?.launch {
            try {
                val apps = loadAppList()
                    .map { loadApp(it) }
                    .filter { it.packageName != app.packageName }
                    .toMutableList()
                apps.sortBy { it.label }
                postValue(Result.success(apps))
            } catch (e: Exception) {
                Timber.e(e, "Error loading packages")
                postValue(Result.failure(e))
            }
        }
    }

    private suspend fun loadAppList() : List<ResolveInfo> =
            withContext(Dispatchers.IO) {
                return@withContext getPackageManager().queryIntentActivities(
                    Intent(
                        Intent.ACTION_MAIN,
                        null
                    ).addCategory(Intent.CATEGORY_LAUNCHER), 0
                )
            }

    private suspend fun loadApp(pack: ResolveInfo) : AppInfo =
            withContext(Dispatchers.IO) {
                val icon = pack.loadIcon(getPackageManager())
                val bgc = IconAdapter.INSTANCE.getBackgroundColor(icon)
                AppInfo(
                        packageName = pack.activityInfo.packageName,
                        icon = icon,
                        backgroundColor = bgc ?: 0xFFC1CC,
                        label = pack.loadLabel(getPackageManager()).toString()
                )
            }
}