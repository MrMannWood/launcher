package com.example.testapp

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val icon: Drawable,
    val label: String
) {
    val lowerLabel = label.toLowerCase()
}