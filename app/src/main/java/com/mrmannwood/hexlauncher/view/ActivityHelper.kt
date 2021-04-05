package com.mrmannwood.hexlauncher.view

import android.app.Activity
import com.mrmannwood.launcher.R

fun Activity.makeFullScreen() {
    window.apply {
        getColor(R.color.black_translucent).let { color ->
            statusBarColor = color
            navigationBarColor = color
        }
    }
}