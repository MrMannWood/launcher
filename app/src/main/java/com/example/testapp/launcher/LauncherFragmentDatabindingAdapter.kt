package com.example.testapp.launcher

import android.graphics.drawable.AdaptiveIconDrawable
import android.view.View

object LauncherFragmentDatabindingAdapter {

    fun getAdaptiveIconVisibility(appInfo: AppInfo) : Int {
        return if (appInfo.icon is AdaptiveIconDrawable) View.VISIBLE else View.GONE
    }

    fun getNonAdaptiveIconVisibility(appInfo: AppInfo) : Int {
        return if (appInfo.icon is AdaptiveIconDrawable) View.GONE else View.VISIBLE
    }

}