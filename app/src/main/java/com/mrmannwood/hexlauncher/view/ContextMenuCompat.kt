package com.mrmannwood.hexlauncher.view

import android.annotation.TargetApi
import android.os.Build
import android.view.View

interface ContextMenuCompat {

    companion object {
        val INSTANCE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NContextMenuCompat()
        } else {
            FallbackContextMenuCompat()
        }
    }

    fun showContextMenu(view: View, x: Float, y: Float)

    @TargetApi(Build.VERSION_CODES.N)
    private class NContextMenuCompat : ContextMenuCompat {
        override fun showContextMenu(view: View, x: Float, y: Float) {
            view.showContextMenu(x, y)
        }
    }

    private class FallbackContextMenuCompat : ContextMenuCompat {
        override fun showContextMenu(view: View, x: Float, y: Float) {
            view.showContextMenu()
        }
    }
}