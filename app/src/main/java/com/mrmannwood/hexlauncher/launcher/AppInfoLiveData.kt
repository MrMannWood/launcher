package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.DeadObjectException
import androidx.core.graphics.get
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mrmannwood.hexlauncher.coroutine.LiveDataWithCoroutineScope
import kotlinx.coroutines.*
import timber.log.Timber

class AppInfoLiveData private constructor(
        private val app: Application
): LiveDataWithCoroutineScope<Result<List<AppInfo>>>() {

    companion object {

        private lateinit var instance: AppInfoLiveData

        fun create(application: Application) {
            instance = AppInfoLiveData(application)
        }

        fun get() : AppInfoLiveData {
            return instance
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            triggerLoad()
        }
    }

    override fun onActive() {
        super.onActive()
        triggerLoad()

        LocalBroadcastManager.getInstance(app).registerReceiver(
            broadcastReceiver,
            IntentFilter(PackageObserverBroadcastReceiver.PACKAGES_CHANGED)
        )
    }

    override fun onInactive() {
        super.onInactive()
        LocalBroadcastManager.getInstance(app).unregisterReceiver(broadcastReceiver)
    }

    private fun getPackageManager() : PackageManager = app.packageManager

    private fun triggerLoad() {
        scope?.launch {
            try {
                val apps = loadAppList()
                    .map { loadApp(it) }
                    .filter { it.packageName != app.packageName }
                    .toMutableList()
                apps.sortBy { it.label }
                postValue(Result.success(apps))
            } catch (e: Exception) {
                Timber.e(e, "Error loading packages")
                postValue(Result.failure(e))
            }
        }
    }

    private suspend fun loadAppList() : List<ResolveInfo> =
            withContext(Dispatchers.IO) {
                return@withContext getPackageManager().queryIntentActivities(
                    Intent(
                        Intent.ACTION_MAIN,
                        null
                    ).addCategory(Intent.CATEGORY_LAUNCHER), 0
                )
            }

    private suspend fun loadApp(pack: ResolveInfo) : AppInfo =
            withContext(Dispatchers.IO) {
                val icon = pack.loadIcon(getPackageManager())
                val bgc = getBackgroundColor(icon)
                AppInfo(
                        packageName = pack.activityInfo.packageName,
                        icon = icon,
                        backgroundColor = bgc ?: 0xFFC1CC,
                        label = pack.loadLabel(getPackageManager()).toString()
                )
            }

    private fun getBackgroundColor(drawable: Drawable) : Int? {
        if (drawable is AdaptiveIconDrawable) {
            val result = drawableToBitmap(drawable.background) { getDominantColor(it) }
            if (result != null) {
                return result
            }
        }
        return drawableToBitmap(drawable) { bitmap -> getDominantColor(bitmap) }
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

    private fun getDominantColor(bitmap: Bitmap) : Int? {
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