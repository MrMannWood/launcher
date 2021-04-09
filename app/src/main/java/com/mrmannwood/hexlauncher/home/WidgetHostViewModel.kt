package com.mrmannwood.hexlauncher.home

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData

class WidgetHostViewModel : ViewModel() {
    val dateWidgetLiveData = MediatorLiveData<Pair<Int?, Int?>>().apply {
        addSource(PreferenceLiveData(PreferenceKeys.Home.Widgets.DATE, PreferenceLiveData.Extractor.IntExtractor)) { slot ->
            value = Pair(slot, value?.second)
        }
        addSource(PreferenceLiveData(PreferenceKeys.Home.Widgets.Gravity.key(PreferenceKeys.Home.Widgets.DATE), PreferenceLiveData.Extractor.IntExtractor)) { gravity ->
            value = Pair(value?.first, gravity)
        }
    }
    val timeWidgetLiveData = MediatorLiveData<Pair<Int?, Int?>>().apply {
        addSource(PreferenceLiveData(PreferenceKeys.Home.Widgets.TIME, PreferenceLiveData.Extractor.IntExtractor)) { slot ->
            value = Pair(slot, value?.second)
        }
        addSource(PreferenceLiveData(PreferenceKeys.Home.Widgets.Gravity.key(PreferenceKeys.Home.Widgets.TIME), PreferenceLiveData.Extractor.IntExtractor)) { gravity ->
            value = Pair(value?.first, gravity)
        }
    }
}