package com.mrmannwood.hexlauncher.nux

import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.settings.PreferencesLiveData

class NUXViewModel : ViewModel() {
    val preferencesLiveData = PreferencesLiveData.get()
}