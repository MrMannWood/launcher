package com.mrmannwood.hexlauncher.icon

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.get

interface IconAdapter {

    companion object {
        val INSTANCE : IconAdapter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            OreoIconAdapter()
        } else {
            DefaultIconAdapter()
        }
    }

    fun isAdaptive(icon : Drawable) : Boolean

    fun getBackgroundColor(icon: Drawable) : Int?

    @TargetApi(Build.VERSION_CODES.O)
    private class OreoIconAdapter : DefaultIconAdapter() {

        override fun isAdaptive(icon : Drawable) = icon is AdaptiveIconDrawable

        override fun getBackgroundColor(icon: Drawable) : Int? {
            if (icon is AdaptiveIconDrawable) {
                val result = drawableToBitmap(icon.background) { getDominantColor(it) }
                if (result != null) {
                    return result
                }
            }
            return super.getBackgroundColor(icon)
        }
    }

    private open class DefaultIconAdapter : IconAdapter {

        override fun isAdaptive(icon : Drawable) = false

        override fun getBackgroundColor(icon: Drawable) : Int? {
            return drawableToBitmap(icon) { bitmap -> getDominantColor(bitmap) }
        }

        fun <T> drawableToBitmap(drawable: Drawable, func: (Bitmap) -> T) : T {
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                return func(drawable.bitmap)
            }

            val (width, height) = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                1 to 1
            } else {
                drawable.intrinsicWidth to drawable.intrinsicHeight
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            val result = func(bitmap)
            bitmap.recycle()
            return result
        }

        fun getDominantColor(bitmap: Bitmap) : Int? {
            val result = intArrayOf(
                0, 0,
                0, 0,
                0, 0
            )
            for (x in 0 until bitmap.width) {
                for (y in 0 until bitmap.height) {
                    val color = bitmap[x, y]
                    if (color ushr 24 != 0xFF) {
                        continue
                    }
                    for (i in 0 until (result.size / 2)) {
                        if (result[i * 2] == 0) {
                            result[i * 2] = color
                            result[i * 2 + 1] = 1
                            break
                        } else if (result[i * 2] == color) {
                            result[i * 2 + 1]++
                            break
                        }
                    }
                }
            }
            var largest = -1
            var color : Int? = null
            for (i in 0 until (result.size / 2)) {
                val count = result[i * 2 + 1]
                val c = result[i * 2]
                if (count > largest) {
                    largest = count
                    color = c
                }
            }
            return color
        }
    }
}