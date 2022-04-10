package com.mrmannwood.hexlauncher.iconpack

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import com.mrmannwood.hexlauncher.executors.PackageManagerExecutor
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.getAppInfoForApps
import java.util.concurrent.atomic.AtomicBoolean

class IconPackLiveData(
    private val application: Application
): LiveData<List<AppInfo>>() {

    companion object {
        private val APP_ICON_ACTIONS = listOf(
            "com.gau.go.launcherex.theme",
            "org.adw.launcher.THEMES"
        )
    }

    private val isActive = AtomicBoolean(false)

    override fun onActive() {
        super.onActive()
        isActive.set(true)
        PackageManagerExecutor.execute {
            val iconPackApps = APP_ICON_ACTIONS
                .flatMap { action ->
                    application.packageManager.queryIntentActivities(
                        Intent(action), PackageManager.GET_META_DATA
                    )
                }
                .map { it.activityInfo.packageName }
                .distinct()
            getAppInfoForApps(application, iconPackApps.distinct()) {
                println("02_MARSHALL: callback $it")
                if (!isActive.get()) return@getAppInfoForApps
                println("02_MARSHALL: posting")
                postValue(it)
            }
        }
    }

    override fun onInactive() {
        isActive.set(false)
        super.onInactive()
    }
}