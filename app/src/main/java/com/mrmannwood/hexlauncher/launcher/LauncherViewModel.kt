package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository

class LauncherViewModel(app: Application) : AndroidViewModel(app) {
    val apps: LiveData<List<AppInfo>> = getAppInfoLiveData(app)
    val enableCategorySearch = PreferencesRepository.getPrefsBlocking(app).watchPref(
        key = PreferenceKeys.Apps.ENABLE_CATEGORY_SEARCH,
        extractor = PreferenceExtractor.BooleanExtractor
    )
    val enableAllAppsSearch = PreferencesRepository.getPrefsBlocking(app).watchPref(
        key = PreferenceKeys.Apps.ENABLE_ALL_APPS_HOT_KEY,
        extractor = PreferenceExtractor.BooleanExtractor
    )
    val leftHandedLayout = PreferencesRepository.getPrefsBlocking(app).watchPref(
        key = PreferenceKeys.User.LEFT_HANDED,
        extractor = PreferenceExtractor.BooleanExtractor
    )
    val openWhenLastApp = PreferencesRepository.getPrefsBlocking(app).watchPref(
        key = PreferenceKeys.Apps.ENABLE_OPEN_WHEN_ONLY_OPTION,
        extractor = PreferenceExtractor.BooleanExtractor
    )
    val enableFuzzySearch = PreferencesRepository.getPrefsBlocking(app).watchPref(
        key = PreferenceKeys.Apps.ENABLE_FUZZY_SEARCH,
        extractor = PreferenceExtractor.BooleanExtractor
    )
}
