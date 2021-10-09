package com.mrmannwood.hexlauncher.textentrydialog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TextEntryDialogViewModel : ViewModel() {
    val completionLiveData = MutableLiveData<String?>()
}