package com.mrmannwood.hexlauncher.applist

import android.content.res.Resources
import android.util.DisplayMetrics

fun calculateNoOfColumnsForAppList(resources: Resources): Int {
    val displayMetrics: DisplayMetrics = resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
    return (screenWidthDp / 75 + 0.5).toInt()
}