package com.mrmannwood.hexlauncher.appcustomize

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.getSingleAppLiveData

class AppCustomizationViewModel(context: Context, componentName: ComponentName) : ViewModel() {
    val app: LiveData<AppInfo?> = getSingleAppLiveData(context.applicationContext, componentName)
}