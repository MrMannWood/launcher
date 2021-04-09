package com.mrmannwood.hexlauncher.home

import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.launcher.AppInfoLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.IntExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.StringExtractor

class HomeViewModel : ViewModel() {
    val appListLiveData = AppInfoLiveData.get()
    val dateWidgetLiveData = PreferenceLiveData(PreferenceKeys.Home.Widgets.DATE, IntExtractor)
    val timeWidgetLiveData = PreferenceLiveData(PreferenceKeys.Home.Widgets.TIME, IntExtractor)
    val swipeRightLiveData = PreferenceLiveData(PreferenceKeys.Gestures.SwipeRight.PACKAGE_NAME, StringExtractor)
    val swipeLeftLiveData = PreferenceLiveData(PreferenceKeys.Gestures.SwipeLeft.PACKAGE_NAME, StringExtractor)
}