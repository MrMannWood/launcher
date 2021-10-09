package com.mrmannwood.hexlauncher.applist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mrmannwood.hexlauncher.typeconverters.CommaSeparatedListTypeConverter

@Entity(tableName = "app_data_decoration")
class AppDataDecoration(
    @PrimaryKey @ColumnInfo(name = "package_name_dec") val packageName: String,
    @ColumnInfo(name = "hidden") val hidden: Boolean = false,
    @ColumnInfo(name = "bgc_override") val bgcOverride: Int? = null,
    @ColumnInfo(name = "background_hidden") val backgroundHidden: Boolean = false,
    @field:TypeConverters(CommaSeparatedListTypeConverter::class)
    @ColumnInfo(name = "tags") val tags: List<String> = emptyList()
)