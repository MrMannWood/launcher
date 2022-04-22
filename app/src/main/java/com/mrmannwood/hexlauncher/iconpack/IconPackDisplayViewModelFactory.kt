package com.mrmannwood.hexlauncher.iconpack

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class IconPackDisplayViewModelFactory(
    context: Context,
    private val componentName: ComponentName
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return IconPackDisplayViewModel(appContext.applicationContext, componentName) as T
    }

}