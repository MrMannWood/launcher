package com.mrmannwood.hexlauncher.applist

import android.content.res.Resources
import android.util.DisplayMetrics
import com.mrmannwood.launcher.R

fun calculateNoOfColumnsForAppList(resources: Resources): Int {
    val displayMetrics: DisplayMetrics = resources.displayMetrics
    return (displayMetrics.widthPixels / resources.getDimension(R.dimen.hex_view_height)).toInt()
}