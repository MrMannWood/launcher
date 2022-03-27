package com.mrmannwood.hexlauncher.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mrmannwood.hexlauncher.launcher.getAppInfoLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor.IntExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor.StringExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Gestures
import com.mrmannwood.hexlauncher.settings.PreferencesRepository.watchPref

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    val appListLiveData = getAppInfoLiveData(application)
    val gestureOpacityLiveData = watchPref(application, Gestures.OPACITY, IntExtractor)
    val swipeNorthWestLiveData = watchPref(application, Gestures.SwipeNorthWest.PACKAGE_NAME, StringExtractor)
    val swipeNorthEastLiveData = watchPref(application, Gestures.SwipeNorthEast.PACKAGE_NAME, StringExtractor)
    val swipeWestLiveData = watchPref(application, Gestures.SwipeWest.PACKAGE_NAME, StringExtractor)
    val swipeEastLiveData = watchPref(application, Gestures.SwipeEast.PACKAGE_NAME, StringExtractor)
    val swipeSouthWestLiveData = watchPref(application, Gestures.SwipeSouthWest.PACKAGE_NAME, StringExtractor)
    val swipeSouthLiveData = watchPref(application, Gestures.SwipeSouth.PACKAGE_NAME, StringExtractor)
    val swipeSouthEastLiveData = watchPref(application, Gestures.SwipeSouthEast.PACKAGE_NAME, StringExtractor)
}