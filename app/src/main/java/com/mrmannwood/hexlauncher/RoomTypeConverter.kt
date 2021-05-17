package com.mrmannwood.hexlauncher

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class RoomTypeConverter {

    @TypeConverter
    fun fromBitmap(value: Bitmap?) : ByteArray? {
        if (value == null) return null
        val stream = ByteArrayOutputStream()
        value.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(value: ByteArray?) : Bitmap? {
        if (value == null) return null
        return BitmapFactory.decodeByteArray(value, 0, value.size)
    }

}