package com.mrmannwood.hexlauncher.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mrmannwood.hexlauncher.launcher.AppInfoLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor.StringExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Gestures
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    val appListLiveData = AppInfoLiveData.get()
    val swipeRightLiveData = PreferenceLiveData(application, Gestures.SwipeRight.PACKAGE_NAME, StringExtractor)
    val swipeLeftLiveData = PreferenceLiveData(application, Gestures.SwipeLeft.PACKAGE_NAME, StringExtractor)
}