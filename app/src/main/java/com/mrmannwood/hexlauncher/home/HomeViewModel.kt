package com.mrmannwood.hexlauncher.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mrmannwood.hexlauncher.launcher.getAppInfoLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor.StringExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Gestures
import com.mrmannwood.hexlauncher.settings.PreferencesRepository.watchPref

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    val appListLiveData = getAppInfoLiveData(application)
    val swipeRightLiveData = watchPref(application, Gestures.SwipeRight.PACKAGE_NAME, StringExtractor)
    val swipeLeftLiveData = watchPref(application, Gestures.SwipeLeft.PACKAGE_NAME, StringExtractor)
}