package com.mrmannwood.applist

import android.content.*
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.Callback
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.UserHandle
import android.os.UserManager
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import timber.log.Timber

class AppListManager(context: Context) {

    companion object {
        private fun getLauncherApps(context: Context): LauncherApps {
            return context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        }

        @MainThread
        fun startAppDetailsActivity(context: Context, launcherItem: LauncherItem, location: Rect) {
            getLauncherApps(context).startAppDetailsActivity(
                launcherItem.componentName, launcherItem.userHandle, location, null
            )
        }
    }

    private val context = context.applicationContext

    fun registerPackagesChangedReceiver(callback: () -> Unit): Callback {
        val launcherAppsCallback = object : Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                Timber.d("AppListManager::onPackageRemoved")
                callback()
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
                Timber.d("AppListManager::onPackageAdded")
                callback()
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                Timber.d("AppListManager::onPackageChanged")
                callback()
            }

            override fun onPackagesAvailable(
                packageNames: Array<String>, user: UserHandle, replacing: Boolean) {
                Timber.d("AppListManager::onPackagesAvailable")
                callback()
            }

            override fun onPackagesUnavailable(
                packageNames: Array<String>,
                user: UserHandle,
                replacing: Boolean
            ) {
                Timber.d("AppListManager::onPackagesUnavailable")
                callback()
            }
        }
        getLauncherApps().registerCallback(launcherAppsCallback)
        return launcherAppsCallback
    }

    fun unregisterPackagesChangedReceiver(callback: Callback) {
        getLauncherApps().unregisterCallback(callback)
    }

    fun registerManagedEventReceiver(callback: () -> Unit): BroadcastReceiver {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                Timber.d("ManagedEventReceiver -> %s", intent.action)
                callback()
            }
        }
        context.registerReceiver(
            receiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
                addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
            }
        )
        return receiver
    }

    fun unregisterManagedEventReceiver(callback: BroadcastReceiver) {
        context.unregisterReceiver(callback)
    }

    @WorkerThread
    fun queryAppList(): List<LauncherItem> {
        Timber.d("queryAppList begin")
        try {
            val launcherApps = getLauncherApps()
            return (context.getSystemService(Context.USER_SERVICE) as UserManager).userProfiles
                .flatMap { launcherApps.getActivityList(null, it) }
                .filter { it.applicationInfo.packageName != context.packageName }
                .map { app -> loadAppDataFromPackageManager(app, context.packageManager) }
                .toList()
        } finally {
            Timber.d("queryAppList complete")
        }
    }

    @WorkerThread
    private fun loadAppDataFromPackageManager(
        info: LauncherActivityInfo,
        pacman: PackageManager
    ): LauncherItem {
        val appInfo = info.applicationInfo
        return LauncherItem(
            packageName = appInfo.packageName,
            componentName = info.componentName,
            userHandle = info.user,
            label = info.label.toString(),
            lastUpdateTime = pacman.getPackageInfo(appInfo.packageName, 0).lastUpdateTime,
            icon = info.getBadgedIcon(0),
            category = appInfo.category
        )
    }

    private fun getLauncherApps(): LauncherApps {
        return getLauncherApps(context)
    }
}