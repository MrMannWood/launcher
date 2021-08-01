package com.mrmannwood.hexlauncher.applist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_data_decoration")
class AppDataDecoration(
    @PrimaryKey @ColumnInfo(name = "package_name_dec") val packageName: String,
    @ColumnInfo(name = "hidden") val hidden: Boolean = false,
    @ColumnInfo(name = "bgc_override") val bgcOverride: Int? = null
)