package com.mrmannwood.hexlauncher.home

import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.launcher.AppInfoLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.StringExtractor

class HomeViewModel : ViewModel() {
    val appListLiveData = AppInfoLiveData.get()
    val slot1LiveData = PreferenceLiveData(PreferenceKeys.Home.Slots.SLOT_1, StringExtractor)
    val slot2LiveData = PreferenceLiveData(PreferenceKeys.Home.Slots.SLOT_2, StringExtractor)
    val slot3LiveData = PreferenceLiveData(PreferenceKeys.Home.Slots.SLOT_3, StringExtractor)
    val slot4LiveData = PreferenceLiveData(PreferenceKeys.Home.Slots.SLOT_4, StringExtractor)
    val slot5LiveData = PreferenceLiveData(PreferenceKeys.Home.Slots.SLOT_5, StringExtractor)
    val slot6LiveData = PreferenceLiveData(PreferenceKeys.Home.Slots.SLOT_6, StringExtractor)
    val slot7LiveData = PreferenceLiveData(PreferenceKeys.Home.Slots.SLOT_7, StringExtractor)
    val slot8LiveData = PreferenceLiveData(PreferenceKeys.Home.Slots.SLOT_8, StringExtractor)
    val swipeRightLiveData = PreferenceLiveData(PreferenceKeys.Gestures.SwipeRight.PACKAGE_NAME, StringExtractor)
    val swipeLeftLiveData = PreferenceLiveData(PreferenceKeys.Gestures.SwipeLeft.PACKAGE_NAME, StringExtractor)
}