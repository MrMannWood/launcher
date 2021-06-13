package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class LauncherViewModel(app: Application): AndroidViewModel(app) {
    val apps: LiveData<List<AppInfo>> = getAppInfoLiveData(app)
}