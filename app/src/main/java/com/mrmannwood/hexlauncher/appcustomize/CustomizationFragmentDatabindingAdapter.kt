package com.mrmannwood.hexlauncher.appcustomize

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import com.mrmannwood.hexlauncher.icon.IconAdapter
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.LauncherFragmentDatabindingAdapter
import com.mrmannwood.launcher.R

object CustomizationFragmentDatabindingAdapter {

    fun getAppName(appInfo: AppInfo?) = LauncherFragmentDatabindingAdapter.getAppName(appInfo)

    fun getAdaptiveIconVisibility(appInfo: AppInfo?) =
        LauncherFragmentDatabindingAdapter.getAdaptiveIconVisibility(appInfo)

    fun getNonAdaptiveIconVisibility(appInfo: AppInfo?) : Int =
        LauncherFragmentDatabindingAdapter.getNonAdaptiveIconVisibility(appInfo)

    fun getForegroundIcon(appInfo: AppInfo?) : Drawable? {
        if (appInfo == null) return null
        return IconAdapter.INSTANCE.getForegroundDrawable(appInfo.icon) ?: appInfo.icon
    }

    fun getBackgroundIcon(appInfo: AppInfo?) : Drawable? {
        if (appInfo == null) return null
        return IconAdapter.INSTANCE.getBackgroundDrawable(appInfo.icon) ?: appInfo.icon
    }

    fun getBackgroundColor(bgcOverride: java.lang.Integer?, appInfo: AppInfo?) : Int {
        return bgcOverride?.toInt() ?: appInfo?.backgroundColor ?: Color.WHITE
    }

    fun getHideButtonText(res: Resources, appInfo: AppInfo?) : CharSequence? {
        if (appInfo == null) return null
        return if (appInfo.hidden) {
            res.getText(R.string.customize_show_app)
        } else {
            res.getText(R.string.customize_hide_app)
        }
    }
}