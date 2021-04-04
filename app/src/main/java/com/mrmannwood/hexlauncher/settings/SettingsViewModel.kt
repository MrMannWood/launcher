package com.mrmannwood.hexlauncher.settings

import android.Manifest
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import com.mrmannwood.hexlauncher.permissions.PermissionsLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.StringExtractor

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val preferencesLiveData = PreferencesLiveData.get()
    val wallpaperLiveData = MultiplePreferenceLiveData(
            mapOf(
                    PreferenceKeys.Wallpaper.APP_NAME to StringExtractor,
                    PreferenceKeys.Wallpaper.PACKAGE_NAME to StringExtractor
            )
    )
    val contactsPermissionLiveData = PermissionsLiveData(application, PreferenceKeys.Contacts.ALLOW_CONTACT_SEARCH, Manifest.permission.READ_CONTACTS)
    val swipeRightLiveData = PreferenceLiveData(PreferenceKeys.Gestures.SwipeRight.APP_NAME, StringExtractor)
    val swipeLeftLiveData = PreferenceLiveData(PreferenceKeys.Gestures.SwipeLeft.APP_NAME, StringExtractor)
}