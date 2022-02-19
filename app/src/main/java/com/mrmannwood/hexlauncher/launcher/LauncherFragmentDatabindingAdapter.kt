package com.mrmannwood.hexlauncher.launcher

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import com.mrmannwood.hexlauncher.icon.IconAdapter
import com.mrmannwood.launcher.R

object LauncherFragmentDatabindingAdapter {

    fun getAppName(appInfo: AppInfo?) = appInfo?.label ?: ""

    fun getAdaptiveIconVisibility(appInfo: AppInfo?) : Int {
        if (appInfo == null) return View.GONE
        return if (IconAdapter.INSTANCE.isAdaptive(appInfo.icon.get())) View.VISIBLE else View.GONE
    }

    fun getNonAdaptiveIconVisibility(appInfo: AppInfo?) : Int {
        if (appInfo == null) return View.GONE
        return if (IconAdapter.INSTANCE.isAdaptive(appInfo.icon.get())) View.GONE else View.VISIBLE
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
        val icon = appInfo.icon.get()
        return IconAdapter.INSTANCE.getForegroundDrawable(icon) ?: icon
    }

    fun getBackgroundIcon(appInfo: AppInfo?) : Drawable? {
        if (appInfo == null) return null
        val icon = appInfo.icon.get()
        return IconAdapter.INSTANCE.getBackgroundDrawable(icon) ?: icon
    }

    fun getBackgroundIconVisibility(appInfo: AppInfo?) : Int {
        if (appInfo == null) return View.GONE
        val icon = appInfo.icon.get()
        if (!IconAdapter.INSTANCE.isAdaptive(icon)) return View.GONE
        return if (appInfo.backgroundHidden) View.GONE else View.VISIBLE
    }

    fun getLabelStartOf(leftHanded: Boolean): Int? = if (leftHanded) null else R.id.icon_parent

    fun getLabelEndOf(leftHanded: Boolean): Int? = if (leftHanded) R.id.icon_parent else null
}