package com.mrmannwood.applist

import android.content.ComponentName
import android.graphics.drawable.Drawable
import android.os.UserHandle

data class LauncherItem (
    val packageName: String,
    val componentName: ComponentName,
    val userHandle: UserHandle,
    val label: String,
    val lastUpdateTime: Long,
    val icon: Drawable,
    val category: Int
)