package com.mrmannwood.hexlauncher.launcher

import android.view.View
import com.mrmannwood.hexlauncher.icon.IconAdapter

object LauncherFragmentDatabindingAdapter {

    fun getAppName(appInfo: AppInfo?) = appInfo?.label ?: ""

    fun getAdaptiveIconVisibility(appInfo: AppInfo?) : Int {
        if (appInfo == null) return View.GONE
        return if (IconAdapter.INSTANCE.isAdaptive(appInfo.icon)) View.VISIBLE else View.GONE
    }

    fun getNonAdaptiveIconVisibility(appInfo: AppInfo?) : Int {
        if (appInfo == null) return View.GONE
        return if (IconAdapter.INSTANCE.isAdaptive(appInfo.icon)) View.GONE else View.VISIBLE
    }

    fun getHiddenVisibility(appInfo: AppInfo?) : Int {
        if (appInfo == null) return View.GONE
        return if (appInfo.hidden) View.VISIBLE else View.GONE
    }
}