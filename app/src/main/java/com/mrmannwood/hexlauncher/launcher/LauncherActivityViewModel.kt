package com.mrmannwood.hexlauncher.launcher

import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.settings.PreferencesLiveData

class LauncherActivityViewModel : ViewModel() {
    val preferencesLiveData = PreferencesLiveData.get()
    val appInfoLiveData = AppInfoLiveData.get()
}