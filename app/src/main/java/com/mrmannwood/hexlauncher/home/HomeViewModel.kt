package com.mrmannwood.hexlauncher.home

import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.launcher.AppInfoLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Gestures
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.StringExtractor

class HomeViewModel : ViewModel() {
    val appListLiveData = AppInfoLiveData.get()
    val swipeRightLiveData = PreferenceLiveData(Gestures.SwipeRight.PACKAGE_NAME, StringExtractor)
    val swipeLeftLiveData = PreferenceLiveData(Gestures.SwipeLeft.PACKAGE_NAME, StringExtractor)
}