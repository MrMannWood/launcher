package com.mrmannwood.hexlauncher

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Pads the bottom of this view by the height of the on-screen keyboard while it is visible.
 *
 * Needed because apps targeting API 35+ get edge-to-edge enforced by the platform: the window no
 * longer auto-resizes around the IME, so content pinned to the bottom (e.g. a search box) would
 * otherwise be laid out behind the keyboard.
 */
fun View.padBottomForKeyboard() {
    val initialPaddingBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        v.updatePadding(bottom = initialPaddingBottom + imeHeight)
        insets
    }
}

fun measureScreen(activity: Activity): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val insets =
            windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.width() - insets.left - insets.right
    } else {
        measureScreenFallback(activity)
    }
}

@Suppress("DEPRECATION")
private fun measureScreenFallback(activity: Activity): Int {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}
