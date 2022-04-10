package com.mrmannwood.hexlauncher.iconpack

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class IconPackDisplayViewModelFactory(
    context: Context,
    private val packageName: String
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return IconPackDisplayViewModel(appContext.applicationContext, packageName) as T
    }

}