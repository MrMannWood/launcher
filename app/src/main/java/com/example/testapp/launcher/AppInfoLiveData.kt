package com.example.testapp.launcher

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.*
import android.graphics.drawable.*
import android.view.Gravity
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.get
import androidx.lifecycle.LiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.palette.graphics.Palette
import com.example.testapp.init
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

class AppInfoLiveData(private val context: Application): LiveData<Result<List<AppInfo>>>() {

    private val pacMan: PackageManager = context.packageManager

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            triggerLoad()
        }
    }

    override fun onActive() {
        println("onActive")
        super.onActive()
        triggerLoad()

        LocalBroadcastManager.getInstance(context).registerReceiver(
            broadcastReceiver,
            IntentFilter(PackageObserverBroadcastReceiver.PACKAGES_CHANGED)
        )
    }

    override fun onInactive() {
        super.onInactive()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver)
    }

    private fun triggerLoad() {
        executor.submit {
            println("executor running")
            val value: Result<List<AppInfo>> = try {
                val packages: List<ResolveInfo> = pacMan.queryIntentActivities(Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0)

                val appList = mutableListOf<AppInfo>()
                for (pack in packages) {
                    val icon = pack.loadIcon(pacMan)
                    val a = AppInfo(
                        packageName = pack.activityInfo.packageName,
                        icon = adaptIcon(icon),
                        backgroundColor = addAlphaToColor(getBackgroundColor(icon)),
                        label = pack.loadLabel(pacMan).toString()
                    )
                    appList.add(a)
                }
                appList.sortBy { it.label }

                success(appList)
            } catch (e: Exception) {
                failure(e)
            }
            postValue(value)
        }
    }

    private fun adaptIcon(drawable: Drawable) : Drawable {
        return if (drawable is AdaptiveIconDrawable) {
            drawable
        } else {
            makeAdaptive(drawable)
        }
    }

    private fun makeAdaptive(drawable: Drawable) : AdaptiveIconDrawable {
        return drawableToBitmap(drawable) { bitmap ->
            AdaptiveIconDrawable(
                ColorDrawable(getBackgroundColor(bitmap)),
                drawable
            ).init {
                setBounds(0, 0, 25, 25)
            }
        }.init {
            setBounds()
        }
    }

    private fun addAlphaToColor(color: Int) : Int = ColorUtils.setAlphaComponent(color, 200)

    private fun getBackgroundColor(drawable: Drawable) : Int =
        drawableToBitmap(drawable) { bitmap -> getBackgroundColor(bitmap) }

    private fun getBackgroundColor(bitmap: Bitmap) : Int =
        Palette.from(bitmap).generate().getDominantColor(bitmap[0, 0])

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