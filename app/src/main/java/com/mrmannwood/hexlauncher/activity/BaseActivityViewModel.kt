package com.mrmannwood.hexlauncher.activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository.watchPref

class BaseActivityViewModel(application: Application) : AndroidViewModel(application) {
    val useHyperlegibleFont = watchPref(
        context = application,
        key = PreferenceKeys.Font.USE_ATKINSON_HYPERLEGIBLE,
        extractor = PreferenceExtractor.BooleanExtractor
    ).asLiveData()
}