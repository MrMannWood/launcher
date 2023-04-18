package com.mrmannwood.hexlauncher.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mrmannwood.hexlauncher.launcher.getAppInfoLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor.IntExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor.StringExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Gestures
import com.mrmannwood.hexlauncher.settings.PreferencesRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    val appListLiveData = getAppInfoLiveData(application)
    val gestureOpacityLiveData = PreferencesRepository.getPrefsBlocking(application)
        .watchPref(Gestures.OPACITY, IntExtractor)
    val swipeNorthWestLiveData = PreferencesRepository.getPrefsBlocking(application)
        .watchPref(Gestures.SwipeNorthWest.PACKAGE_NAME, StringExtractor)
    val swipeNorthEastLiveData = PreferencesRepository.getPrefsBlocking(application)
        .watchPref(Gestures.SwipeNorthEast.PACKAGE_NAME, StringExtractor)
    val swipeWestLiveData = PreferencesRepository.getPrefsBlocking(application)
        .watchPref(Gestures.SwipeWest.PACKAGE_NAME, StringExtractor)
    val swipeEastLiveData = PreferencesRepository.getPrefsBlocking(application)
        .watchPref(Gestures.SwipeEast.PACKAGE_NAME, StringExtractor)
    val swipeSouthWestLiveData = PreferencesRepository.getPrefsBlocking(application)
        .watchPref(Gestures.SwipeSouthWest.PACKAGE_NAME, StringExtractor)
    val swipeSouthLiveData = PreferencesRepository.getPrefsBlocking(application)
        .watchPref(Gestures.SwipeSouth.PACKAGE_NAME, StringExtractor)
    val swipeSouthEastLiveData = PreferencesRepository.getPrefsBlocking(application)
        .watchPref(Gestures.SwipeSouthEast.PACKAGE_NAME, StringExtractor)
}
