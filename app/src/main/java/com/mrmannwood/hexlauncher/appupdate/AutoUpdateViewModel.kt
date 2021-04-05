package com.mrmannwood.hexlauncher.appupdate

import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.AutoUpdate.ALLOW_AUTO_UPDATE
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.BooleanExtractor

class AutoUpdateViewModel : ViewModel() {
    val autoUpdatePrefLiveData = PreferenceLiveData(ALLOW_AUTO_UPDATE, BooleanExtractor)
}