package com.mrmannwood.hexlauncher.role

import android.annotation.TargetApi
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.role.RoleManagerCompat
import com.mrmannwood.hexlauncher.launcher.FakeLauncherActivity
import java.util.*

sealed class RoleManagerHelper {

    companion object {
        val INSTANCE : RoleManagerHelper =
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> QRoleManagerHelper
                else -> HackyAsShitRoleManagerHelper
            }
    }

    enum class RoleManagerResult {
        ROLE_NOT_AVAILABLE,
        ROLE_NOT_HELD,
        ROLE_HELD
    }

    abstract fun getRoleStatus(context: Context, role: String) : RoleManagerResult
    abstract fun getRoleSetIntent(context: Context, role: String) : Pair<Intent, () -> Unit>

    @TargetApi(Build.VERSION_CODES.Q)
    private object QRoleManagerHelper : RoleManagerHelper() {

        override fun getRoleStatus(context: Context, role: String) : RoleManagerResult {
            val roleManager = context.getSystemService(RoleManager::class.java) as RoleManager

            return if (!roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                RoleManagerResult.ROLE_NOT_AVAILABLE
            } else if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                RoleManagerResult.ROLE_HELD
            } else {
                RoleManagerResult.ROLE_NOT_HELD
            }
        }

        override fun getRoleSetIntent(context: Context, role: String) : Pair<Intent, () -> Unit> {
            val roleManager = context.getSystemService(RoleManager::class.java) as RoleManager
            return roleManager.createRequestRoleIntent(role) to { }
        }
    }

    /*
     * Uses legacy interactions to fake role interactions
     * {@see https://stackoverflow.com/questions/27991656/how-to-set-default-app-launcher-programmatically}
     */
    @SuppressWarnings( "deprecation" )
    private object HackyAsShitRoleManagerHelper : RoleManagerHelper() {

        override fun getRoleStatus(context: Context, role: String) : RoleManagerResult {
            if (RoleManagerCompat.ROLE_HOME != role) {
                return RoleManagerResult.ROLE_NOT_AVAILABLE
            }

            val filters = ArrayList<IntentFilter>()
            filters.add(IntentFilter(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) })

            val activities = ArrayList<ComponentName>()
            context.packageManager.getPreferredActivities(filters, activities, null /* packageName */)

            return if (activities.filter { it.packageName == context.packageName }.any()) {
                RoleManagerResult.ROLE_HELD
            } else {
                RoleManagerResult.ROLE_NOT_HELD
            }
        }

        override fun getRoleSetIntent(context: Context, role: String) : Pair<Intent, () -> Unit> {
            if (RoleManagerCompat.ROLE_HOME != role) {
                throw UnsupportedOperationException("Roles other than HOME are not supported")
            }

            val pacman = context.packageManager
            val componentName = ComponentName(context, FakeLauncherActivity::class.java)
            pacman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)

            return Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            } to  {
                pacman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP)
            }
        }
    }

}
