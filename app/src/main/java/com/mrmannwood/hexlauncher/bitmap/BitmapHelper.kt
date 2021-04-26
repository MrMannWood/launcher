package com.mrmannwood.hexlauncher.bitmap

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.mrmannwood.hexlauncher.media.OrientationResult

fun Bitmap.rotate(degrees: Int) : Bitmap = rotate(degrees.toFloat())

fun Bitmap.rotate(degrees: Float) : Bitmap {
    return tryApplyTransform(this, Matrix().apply { setRotate(degrees) })
}

fun Bitmap.rotate(orientation: OrientationResult?) : Bitmap {
    if (orientation == null) return this
    if (orientation is OrientationResult.Degrees) return rotate(orientation.degrees)
    if (orientation !is OrientationResult.Exif) throw UnsupportedOperationException("Unknown type: ${orientation::class.simpleName}")

    val matrix = Matrix()
    when (orientation.exifRotation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> { matrix.apply { setScale(-1f, 1f) } }
        ExifInterface.ORIENTATION_ROTATE_180 -> { matrix.apply { setRotate(180f) } }
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> { matrix.apply { setRotate(180f); postScale(-1f, 1f) } }
        ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.apply { setRotate(90f); postScale(-1f, 1f) }}
        ExifInterface.ORIENTATION_ROTATE_90 -> { matrix.apply { setRotate(90f) } }
        ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.apply { setRotate(-90f); postScale(-1f, 1f) } }
        ExifInterface.ORIENTATION_ROTATE_270 -> { matrix.apply { setRotate(-90f) } }
        else -> return this
    }
    return tryApplyTransform(this, matrix)
}

private fun tryApplyTransform(bitmap: Bitmap, matrix: Matrix) : Bitmap {
    val result : Bitmap
    try {
        result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } catch (e: OutOfMemoryError) {
        return bitmap
    }
    if (result != bitmap) {
        bitmap.recycle()
    }
    return result
}