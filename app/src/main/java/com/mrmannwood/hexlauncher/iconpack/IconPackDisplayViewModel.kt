package com.mrmannwood.hexlauncher.iconpack

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.executors.PackageManagerExecutor
import com.mrmannwood.hexlauncher.executors.cpuBoundTaskExecutor
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.getAppInfoLiveData
import com.mrmannwood.iconpack.IconPackLiveData

class IconPackDisplayViewModel(
    context: Context,
    componentName: ComponentName
) : ViewModel() {
    val installedApps: LiveData<List<AppInfo>> = getAppInfoLiveData(context.applicationContext, false)
    val iconPackLiveData = IconPackLiveData(
        context.applicationContext,
        componentName,
        PackageManagerExecutor,
        cpuBoundTaskExecutor,
        Transformations.map(installedApps) {
            it.map { app -> app.componentName }
        }
    )
}
