package com.mrmannwood.hexlauncher.launcher

import android.graphics.drawable.Drawable
import java.util.*

data class AppInfo(
    val packageName: String,
    val icon: Drawable,
    val backgroundColor: Int,
    val label: String,
    val hidden: Boolean,
    val backgroundHidden: Boolean,
) {
    val lowerLabel = label.lowercase(Locale.ROOT)
    val labelComponents = lowerLabel.split(' ')
}