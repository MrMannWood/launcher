package com.mrmannwood.hexlauncher.settings

import android.Manifest
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mrmannwood.hexlauncher.permissions.PermissionsLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.StringExtractor

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val wallpaperAppNameLiveData = PreferenceLiveData(PreferenceKeys.Wallpaper.APP_NAME, StringExtractor)
    val wallpaperPackageLiveData = PreferenceLiveData(PreferenceKeys.Wallpaper.PACKAGE_NAME, StringExtractor)
    val contactsPermissionLiveData = PermissionsLiveData(application, PreferenceKeys.Contacts.ALLOW_CONTACT_SEARCH, Manifest.permission.READ_CONTACTS)
    val swipeRightLiveData = PreferenceLiveData(PreferenceKeys.Gestures.SwipeRight.APP_NAME, StringExtractor)
    val swipeLeftLiveData = PreferenceLiveData(PreferenceKeys.Gestures.SwipeLeft.APP_NAME, StringExtractor)
}