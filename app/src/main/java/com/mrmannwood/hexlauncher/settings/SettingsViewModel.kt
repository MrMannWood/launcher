package com.mrmannwood.hexlauncher.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor.StringExtractor
import com.mrmannwood.hexlauncher.settings.PreferencesRepository.watchPref

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val swipeRightLiveData = watchPref(application, PreferenceKeys.Gestures.SwipeRight.APP_NAME, StringExtractor).asLiveData()
    val swipeLeftLiveData = watchPref(application, PreferenceKeys.Gestures.SwipeLeft.APP_NAME, StringExtractor).asLiveData()
}