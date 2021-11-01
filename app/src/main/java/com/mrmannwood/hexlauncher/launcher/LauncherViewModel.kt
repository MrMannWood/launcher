package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository.watchPref

class LauncherViewModel(app: Application): AndroidViewModel(app) {
    val apps: LiveData<List<AppInfo>> = getAppInfoLiveData(app)
    val enableCategorySearch = watchPref(
        context = getApplication(),
        key = PreferenceKeys.Apps.ENABLE_CATEGORY_SEARCH,
        extractor = PreferenceExtractor.BooleanExtractor
    ).asLiveData()
}