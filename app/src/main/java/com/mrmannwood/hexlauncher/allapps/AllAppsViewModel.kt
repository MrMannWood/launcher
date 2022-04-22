package com.mrmannwood.hexlauncher.allapps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.getAppInfoLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository

class AllAppsViewModel(app: Application) : AndroidViewModel(app) {
    val apps: LiveData<List<AppInfo>> = getAppInfoLiveData(appContext = app, showHidden = true)
    val leftHandedLayout = PreferencesRepository.watchPref(
        context = getApplication(),
        key = PreferenceKeys.User.LEFT_HANDED,
        extractor = PreferenceExtractor.BooleanExtractor
    )
}
