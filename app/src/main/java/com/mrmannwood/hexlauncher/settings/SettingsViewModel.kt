package com.mrmannwood.hexlauncher.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor.StringExtractor

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val swipeRightLiveData = PreferenceLiveData(application, PreferenceKeys.Gestures.SwipeRight.APP_NAME, StringExtractor)
    val swipeLeftLiveData = PreferenceLiveData(application, PreferenceKeys.Gestures.SwipeLeft.APP_NAME, StringExtractor)
}