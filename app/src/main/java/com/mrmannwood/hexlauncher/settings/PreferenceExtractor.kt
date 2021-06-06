package com.mrmannwood.hexlauncher.settings

import android.content.SharedPreferences

sealed class PreferenceExtractor<T> {

    abstract fun getValue(sharedPreferences: SharedPreferences, key: String) : T?

    object StringExtractor : PreferenceExtractor<String?>() {
        override fun getValue(sharedPreferences: SharedPreferences, key: String): String? {
            return sharedPreferences.getString(key, null)
        }
    }
    abstract class PrimitiveExtractor<T>: PreferenceExtractor<T?>() {
        override fun getValue(sharedPreferences: SharedPreferences, key: String): T? {
            return if (sharedPreferences.contains(key)) {
                extract(sharedPreferences, key)
            } else {
                null
            }
        }
        abstract fun extract(sharedPreferences: SharedPreferences, key: String) : T
    }
    object BooleanExtractor : PrimitiveExtractor<Boolean>() {
        override fun extract(sharedPreferences: SharedPreferences, key: String): Boolean {
            return sharedPreferences.getBoolean(key, false)
        }
    }
    object IntExtractor : PrimitiveExtractor<Int>() {
        override fun extract(sharedPreferences: SharedPreferences, key: String): Int {
            return sharedPreferences.getInt(key, 0)
        }
    }
    object FloatExtractor : PrimitiveExtractor<Float>() {
        override fun extract(sharedPreferences: SharedPreferences, key: String): Float {
            return sharedPreferences.getFloat(key, 0f)
        }
    }
    object LongExtractor : PrimitiveExtractor<Long>() {
        override fun extract(sharedPreferences: SharedPreferences, key: String): Long {
            return sharedPreferences.getLong(key, 0L)
        }
    }
}