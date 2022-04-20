package com.mrmannwood.hexlauncher.applist

import android.content.ComponentName
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mrmannwood.hexlauncher.typeconverters.ManualRoomTypeConverters

// TODO this is going to need a migration
@Entity(tableName = "app_data")
data class AppData (
    @field:TypeConverters(ManualRoomTypeConverters::class)
    @PrimaryKey @ColumnInfo(name = "component_name") val componentName: ComponentName,
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "last_update_time") val lastUpdateTime: Long,
    @ColumnInfo(name = "background_color") val backgroundColor: Int,
    @ColumnInfo(name = "hidden") val hidden: Boolean = false,
    @ColumnInfo(name = "bgc_override") val bgcOverride: Int? = null,
    @ColumnInfo(name = "background_hidden") val backgroundHidden: Boolean = false,
    @field:TypeConverters(ManualRoomTypeConverters::class)
    @ColumnInfo(name = "tags") val tags: List<String> = emptyList()
) {
    data class Timestamp(
        @ColumnInfo(name = "component_name") val componentName: ComponentName,
        @ColumnInfo(name = "last_update_time") val timestamp: Long
    )
}
