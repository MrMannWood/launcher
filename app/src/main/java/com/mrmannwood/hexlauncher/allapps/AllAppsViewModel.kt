package com.mrmannwood.hexlauncher.allapps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.Result
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.AppInfoLiveData

class AllAppsViewModel: ViewModel() {
    val apps: LiveData<Result<List<AppInfo>>> = AppInfoLiveData.get()
}