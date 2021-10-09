package com.mrmannwood.hexlauncher.typeconverters

import androidx.room.TypeConverter

class CommaSeparatedListTypeConverter {
    @TypeConverter
    fun fromString(s: String): List<String> {
        return s.split(',').filter { it.isNotEmpty() }
    }
    @TypeConverter
    fun fromList(l: List<String>): String {
        return l.joinToString(separator = ",") { it }
    }
}