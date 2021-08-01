package com.mrmannwood.hexlauncher.launcher

import android.graphics.Color
import android.graphics.drawable.Drawable
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

    fun getBackgroundColor(appInfo: AppInfo?) : Int {
        return appInfo?.backgroundColor ?: Color.WHITE
    }

    fun getForegroundIcon(appInfo: AppInfo?) : Drawable? {
        if (appInfo == null) return null
        return IconAdapter.INSTANCE.getForegroundDrawable(appInfo.icon) ?: appInfo.icon
    }

    fun getBackgroundIcon(appInfo: AppInfo?) : Drawable? {
        if (appInfo == null) return null
        return IconAdapter.INSTANCE.getBackgroundDrawable(appInfo.icon) ?: appInfo.icon
    }

    fun getBackgroundIconVisibility(appInfo: AppInfo?) : Int {
        if (appInfo == null) return View.GONE
        if (!IconAdapter.INSTANCE.isAdaptive(appInfo.icon)) return View.GONE
        return if (appInfo.backgroundHidden) View.GONE else View.VISIBLE
    }
}