package com.mrmannwood.hexlauncher.appcustomize

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AppCustomizationViewModelFactory(
    private val context: Context,
    private val componentName: ComponentName
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppCustomizationViewModel(context.applicationContext, componentName) as T
    }
}
