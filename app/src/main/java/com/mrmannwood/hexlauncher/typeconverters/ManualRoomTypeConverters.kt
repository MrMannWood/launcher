package com.mrmannwood.hexlauncher.typeconverters

import android.content.ComponentName
import androidx.room.TypeConverter

class ManualRoomTypeConverters {

    @TypeConverter
    fun commaSeparatedListFromString(s: String): List<String> {
        return s.split(',').filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun commaSeperatedListToString(l: List<String>): String {
        return l.joinToString(separator = ",") { it }
    }

    @TypeConverter
    fun componentNameFromString(s: String): ComponentName {
        return ComponentName.unflattenFromString(s)!!
    }

    @TypeConverter
    fun componentNameToString(name: ComponentName): String {
        return name.flattenToString()
    }
}