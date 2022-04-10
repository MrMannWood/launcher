package com.mrmannwood.hexlauncher.iconpack

import android.content.Context
import androidx.lifecycle.ViewModel

class IconPackDisplayViewModel(
    context: Context,
    packageName: String
): ViewModel() {
    val iconPackLiveData = IconPackLiveData(context.applicationContext, packageName)
}