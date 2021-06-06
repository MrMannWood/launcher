package com.mrmannwood.hexlauncher.home

import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.settings.PreferencesLiveData

class HomeArrangementViewModel : ViewModel() {
    val preferencesLiveData = PreferencesLiveData.get()
}