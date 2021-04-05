package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mrmannwood.hexlauncher.contacts.ContactsLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.BooleanExtractor

class LauncherViewModel(app: Application): AndroidViewModel(app) {

    val apps: LiveData<Result<List<AppInfo>>> = AppInfoLiveData.get(getApplication())
    val contacts = ContactsLiveData(app)
    val showAllAppsPreferenceLiveData = PreferenceLiveData(PreferenceKeys.AppList.SHOW_ALL_APPS, BooleanExtractor)

}