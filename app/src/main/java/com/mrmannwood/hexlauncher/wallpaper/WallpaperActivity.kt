package com.mrmannwood.hexlauncher.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mrmannwood.hexlauncher.bitmap.rotate
import com.mrmannwood.hexlauncher.media.MediaOrientationReader
import com.mrmannwood.launcher.R
import timber.log.Timber
import java.io.IOException
import kotlin.math.abs

class WallpaperActivity : AppCompatActivity() {

    companion object {
        private const val URI_KEY = "uri"

        fun Activity.makeWallpaperActivityIntent(uri: Uri) : Intent {
            return Intent(this, WallpaperActivity::class.java).apply {
                putExtra(URI_KEY, uri.toString())
            }
        }
    }

    private lateinit var photoView : PhotoView

    private var screenWidth : Int = 0
    private var screenHeight : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper)
        supportActionBar?.hide()

        val originalBitmap = tryLoadBitmapFromIntent()
        if (originalBitmap == null) {
            finish()
            return
        }

        val container = findViewById<View>(R.id.container)
        photoView = findViewById(R.id.photo_view)
        findViewById<Button>(R.id.set_wallpaper_button).apply {
            setOnClickListener {
                val bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888).let { bitmap ->
                    Canvas(bitmap).apply {
                        val original = (photoView.drawable as BitmapDrawable).bitmap

                        val ratio = (1.0 * original.height) / photoView.height

                        val displayRect = photoView.displayRect
                        val left = abs(displayRect.left).toInt()
                        val top = abs(displayRect.top).toInt()
                        val right = left + photoView.width
                        val bottom = top + photoView.height

                        drawBitmap(
                            original,
                            Rect(
                                (left * ratio).toInt(),
                                (top * ratio).toInt(),
                                (right * ratio).toInt(),
                                (bottom * ratio).toInt()
                            ),
                            Rect(0, 0, screenWidth, screenHeight),
                            Paint().apply { colorFilter = photoView.colorFilter }
                        )
                    }
                    bitmap
                }
                WallpaperManager.getInstance(this@WallpaperActivity).setBitmap(bitmap)
                finish()
            }
        }

        container.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                if (left == 0 && top == 0 && right == 0 && bottom == 0) return
                container.removeOnLayoutChangeListener(this)

                getScreenSize { x, y ->
                    screenWidth = x
                    screenHeight = y

                    val params = photoView.layoutParams as FrameLayout.LayoutParams

                    val ratio = y.toDouble() / x
                    params.width = (container.height / ratio).toInt()
                    params.height = container.height

                    photoView.layoutParams = params
                }
            }
        })

        findViewById<SwitchMaterial>(R.id.grayscale).apply {
            setOnCheckedChangeListener { _, isChecked ->
                enableGrayscale(isChecked)
            }
            enableGrayscale(isChecked)
        }

        photoView.setImageBitmap(originalBitmap)
    }

    private fun tryLoadBitmapFromIntent() : Bitmap? {
        val uri = Uri.parse(intent.getStringExtra(URI_KEY) ?: return null)
        return try {
            BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                .rotate(MediaOrientationReader.INSTANCE.getOrientation(uri, contentResolver))
        } catch (e: IOException) {
            Timber.e(e, "Unable to extract image from result")
            null
        }
    }

    private fun getScreenSize(func: (x: Int, y: Int) -> Unit) {
        val point = Point()
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display!!
        } else {
            windowManager.defaultDisplay
        }
        display.getRealSize(point)

        func(point.x, point.y)
    }

    private fun enableGrayscale(enabled: Boolean) {
        photoView.colorFilter = ColorMatrixColorFilter(
            ColorMatrix().apply {
                setSaturation(if (enabled) 0f else 1f)
            }
        )
    }
}