package com.mrmannwood.applist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.Callback
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
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

        /**
         * Android 16 blocks explicit component intents that do not match the target
         * activity's <intent-filter> (e.g. a bare component intent), unless the intent
         * was created by the platform. LauncherApps is the designated launcher API, and
         * is required (rather than optional) for launching an app that lives in a
         * different profile (e.g. a Private Space or work profile app) than the caller,
         * since a plain startActivity() call can never cross a user/profile boundary,
         * regardless of role status.
         *
         * A [LauncherItem] in the caller's own profile can always be launched via
         * LauncherApps, independent of whether the launcher role is held, so a
         * SecurityException in that case falls back to a filter-matching intent. A
         * SecurityException for a [LauncherItem] in a different profile means there is no
         * permitted way for this call to succeed right now (the role is not held, or the
         * target profile is unavailable/locked) - that is reported back as
         * [LaunchResult.CrossProfileAccessDenied] so the caller can decide what to tell
         * the user, rather than attempting a fallback intent that cannot work.
         */
        @MainThread
        fun startMainActivity(
            context: Context,
            launcherItem: LauncherItem,
            sourceBounds: Rect? = null
        ): LaunchResult {
            return try {
                getLauncherApps(context).startMainActivity(
                    launcherItem.componentName, launcherItem.userHandle, sourceBounds, null
                )
                LaunchResult.Success
            } catch (e: SecurityException) {
                if (launcherItem.userHandle != Process.myUserHandle()) {
                    LaunchResult.CrossProfileAccessDenied
                } else {
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_LAUNCHER)
                                component = launcherItem.componentName
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                        LaunchResult.Success
                    } catch (fallbackException: Exception) {
                        LaunchResult.Failure(fallbackException)
                    }
                }
            } catch (e: Exception) {
                LaunchResult.Failure(e)
            }
        }
    }

    sealed class LaunchResult {
        /** The app was launched (or a launch request was successfully dispatched). */
        object Success : LaunchResult()

        /**
         * The target app lives in a different profile than the caller, and there is no
         * permitted way to launch it right now - either the default launcher role is not
         * held, or the target profile is unavailable (e.g. locked). Callers should check
         * [com.mrmannwood.hexlauncher.role.RoleManagerHelper] to distinguish the two and
         * show an appropriate message.
         */
        object CrossProfileAccessDenied : LaunchResult()

        /** Launching failed for a reason unrelated to cross-profile access. */
        data class Failure(val exception: Exception) : LaunchResult()
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
                packageNames: Array<String>,
                user: UserHandle,
                replacing: Boolean
            ) {
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
        registerReceiver(
            context,
            receiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
                addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
            },
            RECEIVER_NOT_EXPORTED,
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
            lastUpdateTime = try {
                pacman.getPackageInfo(appInfo.packageName, 0).lastUpdateTime
            } catch (e: Exception) { -1 /* force insert/replace */ },
            icon = info.getBadgedIcon(0),
            category = appInfo.category
        )
    }

    private fun getLauncherApps(): LauncherApps {
        return getLauncherApps(context)
    }
}
