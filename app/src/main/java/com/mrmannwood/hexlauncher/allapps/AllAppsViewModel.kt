package com.mrmannwood.hexlauncher.allapps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.getAppInfoLiveData

class AllAppsViewModel(app: Application): AndroidViewModel(app) {
    val apps: LiveData<List<AppInfo>> = getAppInfoLiveData(appContext = app, showHidden = true)
}