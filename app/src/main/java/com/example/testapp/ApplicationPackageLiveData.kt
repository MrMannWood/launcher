package com.example.testapp

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.get
import androidx.lifecycle.LiveData
import androidx.palette.graphics.Palette
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sqrt


class ApplicationPackageLiveData(private val pacMan: PackageManager): LiveData<LoadResult<List<AppInfo>, Exception>>() {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    override fun onActive() {
        println("onActive")
        super.onActive()
        executor.submit {
            println("executor running")
            val value: LoadResult<List<AppInfo>, Exception> = try {
                val packages: List<ResolveInfo> = pacMan.queryIntentActivities(Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0)

                val appList = mutableListOf<AppInfo>()
                for (pack in packages) {
                    val icon = pack.loadIcon(pacMan)
                    val a = AppInfo(
                        packageName = pack.activityInfo.packageName,
                        icon = icon,
                        backgroundColor = getBackgroundColor(icon),
                        label = pack.loadLabel(pacMan).toString()
                    )
                    appList.add(a)
                }
                appList.sortBy { it.label }

                LoadResult.Result(appList)
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
            postValue(value)
        }
    }

    private fun getBackgroundColor(drawable: Drawable) : Int =
        drawableToBitmap(getBackgroundDrawable(drawable)) { backgroundBitmap ->
            drawableToBitmap(getForegroundDrawable(drawable)) { foregroundBitmap ->
                val foregroundPalette = Palette.from(foregroundBitmap).generate()
                val backgroundPalette = Palette.from(backgroundBitmap).generate()
                foregroundPalette.getDominantColor(backgroundPalette.getDominantColor(foregroundBitmap[0, 0]))
            }
        }

    private fun getForegroundDrawable(drawable: Drawable) : Drawable {
        return if (drawable is AdaptiveIconDrawable) {
            drawable.foreground
        } else {
            drawable
        }
    }

    private fun getBackgroundDrawable(drawable: Drawable) : Drawable {
        return if (drawable is AdaptiveIconDrawable) {
            drawable.background
        } else {
            drawable
        }
    }

    private fun <T> drawableToBitmap(drawable: Drawable, func: (Bitmap) -> T) : T {
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

    private fun convertToGrayscale(drawable: Drawable): Drawable {
//        val matrix = ColorMatrix()
//        matrix.setSaturation(.05f)
//        val filter = ColorMatrixColorFilter(matrix)
//        drawable.colorFilter = filter
        return drawable
    }
}