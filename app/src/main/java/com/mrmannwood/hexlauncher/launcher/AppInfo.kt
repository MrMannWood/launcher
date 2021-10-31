package com.mrmannwood.hexlauncher.launcher

import android.graphics.drawable.Drawable
import java.util.*

enum class SearchTermType {
    FullName,
    Label,
    Tag,
    Category,
}

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
    val searchTerms : Map<String, SearchTermType> = (
            categories.map { it to SearchTermType.Category } +
                    tags.map { it to SearchTermType.Tag } +
                    lowerLabel.split(' ').map { it to SearchTermType.Label }
            ).toMap()
}