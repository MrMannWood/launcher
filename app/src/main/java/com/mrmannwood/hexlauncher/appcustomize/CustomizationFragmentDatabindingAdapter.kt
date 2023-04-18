package com.mrmannwood.hexlauncher.appcustomize

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.LauncherFragmentDatabindingAdapter
import com.mrmannwood.launcher.R

object CustomizationFragmentDatabindingAdapter {

    fun getAppName(appInfo: AppInfo?) = LauncherFragmentDatabindingAdapter.getAppName(appInfo)

    fun getAdaptiveIconVisibility(appInfo: AppInfo?) =
        LauncherFragmentDatabindingAdapter.getAdaptiveIconVisibility(appInfo)

    fun getNonAdaptiveIconVisibility(appInfo: AppInfo?): Int =
        LauncherFragmentDatabindingAdapter.getNonAdaptiveIconVisibility(appInfo)

    fun getForegroundIcon(appInfo: AppInfo?) =
        LauncherFragmentDatabindingAdapter.getForegroundIcon(appInfo)

    fun getBackgroundIcon(appInfo: AppInfo?) =
        LauncherFragmentDatabindingAdapter.getBackgroundIcon(appInfo)

    fun getBackgroundColor(bgcOverride: java.lang.Integer?, appInfo: AppInfo?): Int {
        return bgcOverride?.toInt()
            ?: LauncherFragmentDatabindingAdapter.getBackgroundColor(appInfo)
    }

    fun getHideButtonText(res: Resources, appInfo: AppInfo?): CharSequence? {
        if (appInfo == null) return null
        return if (appInfo.hidden) {
            res.getText(R.string.customize_show_app)
        } else {
            res.getText(R.string.customize_hide_app)
        }
    }

    fun getBackgroundIconVisibility(appInfo: AppInfo?): Int =
        LauncherFragmentDatabindingAdapter.getBackgroundIconVisibility(appInfo)

    fun getBackgroundVisibilityIcon(res: Resources, appInfo: AppInfo?): Drawable? {
        return when {
            appInfo == null -> {
                null
            }
            appInfo.backgroundHidden -> {
                ResourcesCompat.getDrawable(res, R.drawable.outline_visibility_off, null)
            }
            else -> {
                ResourcesCompat.getDrawable(res, R.drawable.outline_visibility, null)
            }
        }
    }

    fun getTagRemoveButtonContentDescription(res: Resources, tag: String): String {
        return res.getString(R.string.customize_app_tags_delete_tag_content_description, tag)
    }
}
