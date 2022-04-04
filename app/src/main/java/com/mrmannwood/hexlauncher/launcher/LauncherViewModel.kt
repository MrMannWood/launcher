package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository.watchPref

class LauncherViewModel(app: Application): AndroidViewModel(app) {
    val apps: LiveData<List<AppInfo>> = getAppInfoLiveData(app)
    val enableCategorySearch = watchPref(
        context = getApplication(),
        key = PreferenceKeys.Apps.ENABLE_CATEGORY_SEARCH,
        extractor = PreferenceExtractor.BooleanExtractor
    )
    val enableAllAppsSearch = watchPref(
        context = getApplication(),
        key = PreferenceKeys.Apps.ENABLE_ALL_APPS_HOT_KEY,
        extractor = PreferenceExtractor.BooleanExtractor
    )
    val leftHandedLayout = watchPref(
        context = getApplication(),
        key = PreferenceKeys.User.LEFT_HANDED,
        extractor = PreferenceExtractor.BooleanExtractor
    )
}