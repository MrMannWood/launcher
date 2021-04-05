package com.mrmannwood.hexlauncher.launcher

import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.BooleanExtractor


class HomeViewModel : ViewModel() {
    val appListLiveData = AppInfoLiveData.get()
    val showDateLiveData = PreferenceLiveData(PreferenceKeys.Home.SHOW_DATE, BooleanExtractor)
    val showTimeLiveData = PreferenceLiveData(PreferenceKeys.Home.SHOW_TIME, BooleanExtractor)
    val swipeRightLiveData = PreferenceLiveData(PreferenceKeys.Gestures.SwipeRight.PACKAGE_NAME, PreferenceLiveData.Extractor.StringExtractor)
    val swipeLeftLiveData = PreferenceLiveData(PreferenceKeys.Gestures.SwipeLeft.PACKAGE_NAME, PreferenceLiveData.Extractor.StringExtractor)
}