package com.mrmannwood.iconpack

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import java.util.concurrent.Executor

class IconPackAppLiveData(
    context: Context,
    private val packageManagerExecutor: Executor
) : LiveData<List<String>>() {

    companion object {
        private val APP_ICON_ACTIONS = listOf(
            "com.gau.go.launcherex.theme",
            "org.adw.launcher.THEMES"
        )
    }

    private val context = context.applicationContext

    override fun onActive() {
        super.onActive()
        packageManagerExecutor.execute {
            val pacman = context.packageManager ?: return@execute
            postValue(
                APP_ICON_ACTIONS
                    .flatMap { action ->
                        pacman.queryIntentActivities(Intent(action), PackageManager.GET_META_DATA)
                    }
                    .map { it.activityInfo.packageName }
                    .distinct()
            )
        }
    }
}
