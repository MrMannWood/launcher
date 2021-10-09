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
    val categories: List<String>,
    val tags: List<String>
) {
    val lowerLabel = label.lowercase(Locale.ROOT)
    val searchTerms = lowerLabel.split(' ') + categories + tags
}