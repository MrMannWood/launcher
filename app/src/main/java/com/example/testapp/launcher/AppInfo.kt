package com.example.testapp.launcher

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val icon: Drawable,
    val backgroundColor: Int,
    val label: String
) {
    val lowerLabel = label.toLowerCase()
    val labelComponents = lowerLabel.split(' ')
}