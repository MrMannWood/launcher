package com.mrmannwood.hexlauncher.launcher

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import com.mrmannwood.hexlauncher.icon.IconAdapter
import com.mrmannwood.launcher.R

object LauncherFragmentDatabindingAdapter {

    fun getAppName(hexItem: HexItem?) = hexItem?.label ?: ""

    fun getAdaptiveIconVisibility(hexItem: HexItem?): Int {
        if (hexItem == null) return View.GONE
        val icon = hexItem.icon.get() ?: return View.GONE
        return if (IconAdapter.INSTANCE.isAdaptive(icon)) View.VISIBLE else View.GONE
    }

    fun getNonAdaptiveIconVisibility(hexItem: HexItem?): Int {
        if (hexItem == null) return View.GONE
        val icon = hexItem.icon.get() ?: return View.VISIBLE
        return if (IconAdapter.INSTANCE.isAdaptive(icon)) View.GONE else View.VISIBLE
    }

    fun getHiddenVisibility(hexItem: HexItem?): Int {
        if (hexItem == null) return View.GONE
        return if (hexItem.hidden) View.VISIBLE else View.GONE
    }

    fun getBackgroundColor(hexItem: HexItem?): Int {
        return hexItem?.backgroundColor ?: Color.WHITE
    }

    fun getForegroundIcon(context: Context, hexItem: HexItem?) : Drawable? {
        if (hexItem == null) return null
        val icon = hexItem.icon.get() ?: return getDefaultDrawable(context)
        return IconAdapter.INSTANCE.getForegroundDrawable(icon) ?: icon
    }

    fun getBackgroundIcon(hexItem: HexItem?): Drawable? {
        if (hexItem == null) return null
        val icon = hexItem.icon.get() ?: return null
        return IconAdapter.INSTANCE.getBackgroundDrawable(icon) ?: icon
    }

    fun getBackgroundIconVisibility(hexItem: HexItem?): Int {
        if (hexItem == null) return View.GONE
        val icon = hexItem.icon.get() ?: return View.GONE
        if (!IconAdapter.INSTANCE.isAdaptive(icon)) return View.GONE
        return if (hexItem.backgroundHidden) View.GONE else View.VISIBLE
    }

    fun getLabelStartOf(leftHanded: Boolean): Int? = if (leftHanded) null else R.id.icon_parent

    fun getLabelEndOf(leftHanded: Boolean): Int? = if (leftHanded) R.id.icon_parent else null
<<<<<<< HEAD
}
=======

    private fun getDefaultDrawable(context: Context): Drawable? {
        return ContextCompat.getDrawable(context, R.drawable.outline_question_mark)
    }
}
>>>>>>> 9324413 (can load and show all icons in an icon pack)
