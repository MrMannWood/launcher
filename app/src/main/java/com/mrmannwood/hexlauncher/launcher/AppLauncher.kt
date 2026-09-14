package com.mrmannwood.hexlauncher.launcher

import android.content.Context
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.core.role.RoleManagerCompat
import com.mrmannwood.applist.AppListManager
import com.mrmannwood.hexlauncher.role.RoleManagerHelper
import com.mrmannwood.hexlauncher.role.RoleManagerHelper.RoleManagerResult.ROLE_HELD
import com.mrmannwood.launcher.R
import timber.log.Timber

/**
 * Launches an app on behalf of the two app-selection entry points (the home screen gesture
 * wheel and the full app list), and translates an [AppListManager.LaunchResult] into the
 * appropriate log entry / user-facing message.
 */
object AppLauncher {

    @MainThread
    fun launch(context: Context, appInfo: AppInfo) {
        when (val result = AppListManager.startMainActivity(context, appInfo.launcherItem)) {
            is AppListManager.LaunchResult.Success -> Unit
            is AppListManager.LaunchResult.CrossProfileAccessDenied -> {
                val roleHeld = RoleManagerHelper.INSTANCE.getRoleStatus(
                    context,
                    RoleManagerCompat.ROLE_HOME
                ) == ROLE_HELD
                Timber.w(
                    "Unable to open app in another profile (roleHeld=$roleHeld): ${appInfo.componentName}"
                )
                Toast.makeText(
                    context,
                    if (roleHeld) {
                        R.string.error_app_profile_unavailable
                    } else {
                        R.string.error_app_requires_default_launcher
                    },
                    Toast.LENGTH_LONG
                ).show()
            }
            is AppListManager.LaunchResult.Failure -> {
                Timber.e(result.exception, "Unable to open app: ${appInfo.componentName}")
                Toast.makeText(context, R.string.unable_to_start_app, Toast.LENGTH_LONG).show()
            }
        }
    }
}
