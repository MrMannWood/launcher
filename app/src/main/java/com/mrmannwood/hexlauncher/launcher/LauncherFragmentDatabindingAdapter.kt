package com.mrmannwood.hexlauncher.launcher

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import com.mrmannwood.hexlauncher.icon.IconAdapter
import com.mrmannwood.launcher.R

object LauncherFragmentDatabindingAdapter {

    fun getAppName(hexItem: HexItem?) = hexItem?.label ?: ""

    fun getAdaptiveIconVisibility(hexItem: HexItem?) : Int {
        if (hexItem == null) return View.GONE
        return if (IconAdapter.INSTANCE.isAdaptive(hexItem.icon.get())) View.VISIBLE else View.GONE
    }

    fun getNonAdaptiveIconVisibility(hexItem: HexItem?) : Int {
        if (hexItem == null) return View.GONE
        return if (IconAdapter.INSTANCE.isAdaptive(hexItem.icon.get())) View.GONE else View.VISIBLE
    }

    fun getHiddenVisibility(hexItem: HexItem?) : Int {
        if (hexItem == null) return View.GONE
        return if (hexItem.hidden) View.VISIBLE else View.GONE
    }

    fun getBackgroundColor(hexItem: HexItem?) : Int {
        return hexItem?.backgroundColor ?: Color.WHITE
    }

    fun getForegroundIcon(hexItem: HexItem?) : Drawable? {
        if (hexItem == null) return null
        val icon = hexItem.icon.get()
        return IconAdapter.INSTANCE.getForegroundDrawable(icon) ?: icon
    }

    fun getBackgroundIcon(hexItem: HexItem?) : Drawable? {
        if (hexItem == null) return null
        val icon = hexItem.icon.get()
        return IconAdapter.INSTANCE.getBackgroundDrawable(icon) ?: icon
    }

    fun getBackgroundIconVisibility(hexItem: HexItem?) : Int {
        if (hexItem == null) return View.GONE
        val icon = hexItem.icon.get()
        if (!IconAdapter.INSTANCE.isAdaptive(icon)) return View.GONE
        return if (hexItem.backgroundHidden) View.GONE else View.VISIBLE
    }

    fun getLabelStartOf(leftHanded: Boolean): Int? = if (leftHanded) null else R.id.icon_parent

    fun getLabelEndOf(leftHanded: Boolean): Int? = if (leftHanded) R.id.icon_parent else null
}