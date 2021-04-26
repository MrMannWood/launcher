package com.mrmannwood.hexlauncher.media

import android.annotation.TargetApi
import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface

sealed class OrientationResult {
    data class Degrees(val degrees: Int): OrientationResult()
    data class Exif(val exifRotation: Int): OrientationResult()
}

sealed class MediaOrientationReader {

    companion object {
        val INSTANCE : MediaOrientationReader =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                QMediaOrientationReader
            else
                FallbackMediaOrientationReader
    }

    abstract fun getOrientation(uri: Uri, contentResolver: ContentResolver) : OrientationResult?

    @TargetApi(Build.VERSION_CODES.Q)
    private object QMediaOrientationReader : MediaOrientationReader() {
        override fun getOrientation(uri: Uri, contentResolver: ContentResolver): OrientationResult? {
            return getOrientationFromContentResolver(uri, contentResolver) ?: FallbackMediaOrientationReader.getOrientation(uri, contentResolver)
        }

        private fun getOrientationFromContentResolver(uri: Uri, contentResolver: ContentResolver): OrientationResult? {
            return contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.ORIENTATION),
                null,
                null,
                null
            )?.use {
                if(it.moveToFirst()) {
                    OrientationResult.Degrees(it.getInt(it.getColumnIndex(MediaStore.Images.Media.ORIENTATION)))
                } else {
                    null
                }
            }
        }
    }

    private object FallbackMediaOrientationReader : MediaOrientationReader() {
        override fun getOrientation(uri: Uri, contentResolver: ContentResolver): OrientationResult? {
            return contentResolver.openInputStream(uri)?.use { inputStream ->
                ExifInterface(inputStream).getAttribute(ExifInterface.TAG_ORIENTATION)?.let { result ->
                    OrientationResult.Exif(result.toInt())
                }
            }
        }
    }
}
