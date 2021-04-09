package com.mrmannwood.hexlauncher.home

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Home.Widgets.Gravity
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Home.Widgets.Color
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.IntExtractor

class WidgetHostViewModel : ViewModel() {
    val dateWidgetLiveData = MediatorLiveData<Triple<Int?, Int?, Int?>>().apply {
        addSource(PreferenceLiveData(PreferenceKeys.Home.Widgets.DATE, IntExtractor)) { slot ->
            value = Triple(slot, value?.second, value?.third)
        }
        addSource(PreferenceLiveData(Gravity.key(PreferenceKeys.Home.Widgets.DATE), IntExtractor)) { gravity ->
            value = Triple(value?.first, gravity, value?.third)
        }
        addSource(PreferenceLiveData(Color.key(PreferenceKeys.Home.Widgets.DATE), IntExtractor)) { color ->
            value = Triple(value?.first, value?.second, color)
        }
    }
    val timeWidgetLiveData = MediatorLiveData<Triple<Int?, Int?, Int?>>().apply {
        addSource(PreferenceLiveData(PreferenceKeys.Home.Widgets.TIME, IntExtractor)) { slot ->
            value = Triple(slot, value?.second, value?.third)
        }
        addSource(PreferenceLiveData(Gravity.key(PreferenceKeys.Home.Widgets.TIME), IntExtractor)) { gravity ->
            value = Triple(value?.first, gravity, value?.third)
        }
        addSource(PreferenceLiveData(Color.key(PreferenceKeys.Home.Widgets.TIME), IntExtractor)) { color ->
            value = Triple(value?.first, value?.second, color)
        }
    }
}