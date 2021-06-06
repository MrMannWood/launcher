package com.mrmannwood.hexlauncher.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.StringExtractor

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val swipeRightLiveData = PreferenceLiveData(PreferenceKeys.Gestures.SwipeRight.APP_NAME, StringExtractor)
    val swipeLeftLiveData = PreferenceLiveData(PreferenceKeys.Gestures.SwipeLeft.APP_NAME, StringExtractor)
}