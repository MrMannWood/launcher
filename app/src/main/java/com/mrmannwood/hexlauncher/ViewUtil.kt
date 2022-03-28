package com.mrmannwood.hexlauncher

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets

fun measureScreen(activity: Activity): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
        windowMetrics.bounds.width() - insets.left - insets.right;
    } else {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}