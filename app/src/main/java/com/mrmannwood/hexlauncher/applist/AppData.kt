package com.mrmannwood.hexlauncher.applist

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_data")
data class AppData(
    @PrimaryKey @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "last_update_time") val lastUpdateTime: Long,
    @ColumnInfo(name = "background_color") val backgroundColor: Int,
    @ColumnInfo(name = "icon_foreground", typeAffinity = ColumnInfo.BLOB) val foreground: Bitmap?,
    @ColumnInfo(name = "icon_background", typeAffinity = ColumnInfo.BLOB) val background: Bitmap,
)
