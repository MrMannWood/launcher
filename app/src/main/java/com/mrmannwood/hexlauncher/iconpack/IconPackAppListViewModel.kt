package com.mrmannwood.hexlauncher.iconpack

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository

class IconPackAppListViewModel(application: Application): AndroidViewModel(application) {
    val iconPackAppsLiveData = IconPackAppListLiveData(getApplication())
    val leftHandedLayout = PreferencesRepository.watchPref(
        context = getApplication(),
        key = PreferenceKeys.User.LEFT_HANDED,
        extractor = PreferenceExtractor.BooleanExtractor
    )
}