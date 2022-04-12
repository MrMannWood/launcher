package com.mrmannwood.hexlauncher.iconpack

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.getAppInfoLiveData

class IconPackDisplayViewModel(
    context: Context,
    packageName: String
): ViewModel() {
    val iconPackLiveData = IconPackLiveData(context.applicationContext, packageName)
    val installedApps: LiveData<List<AppInfo>> = getAppInfoLiveData(context.applicationContext, false)
}