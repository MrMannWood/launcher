package com.mrmannwood.hexlauncher.iconpack

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.getAppInfoLiveData

class IconPackDisplayViewModel(
    context: Context,
    componentName: ComponentName
): ViewModel() {
    val iconPackLiveData = IconPackLiveData(context.applicationContext, componentName)
    val installedApps: LiveData<List<AppInfo>> = getAppInfoLiveData(context.applicationContext, false)
}