package com.mrmannwood.hexlauncher.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.mrmannwood.hexlauncher.launcher.getAppInfoLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor.StringExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Gesture
import com.mrmannwood.hexlauncher.settings.PreferencesRepository.watchPref

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    val appListLiveData = getAppInfoLiveData(application)
    val swipeRightLiveData = watchPref(application, Gesture.SwipeRight.PACKAGE_NAME, StringExtractor).asLiveData()
    val swipeLeftLiveData = watchPref(application, Gesture.SwipeLeft.PACKAGE_NAME, StringExtractor).asLiveData()
}