package com.mrmannwood.hexlauncher.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.settings.PreferencesLiveData

class HomeArrangementViewModel : ViewModel() {
    val preferencesLiveData = PreferencesLiveData.get()
    val widgetLiveData = MutableLiveData<String>()
}