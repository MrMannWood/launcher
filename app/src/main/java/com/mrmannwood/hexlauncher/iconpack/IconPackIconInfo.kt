package com.mrmannwood.hexlauncher.iconpack

import android.graphics.drawable.Drawable
import com.mrmannwood.hexlauncher.launcher.Provider

class IconPackIconInfo(
    val ownerPackageName: String,
    val componentName: String,
    val drawableName: String,
    val drawableProvider: Provider<Drawable?>
)