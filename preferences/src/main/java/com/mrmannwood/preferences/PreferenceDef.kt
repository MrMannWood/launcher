package com.mrmannwood.preferences

data class PreferenceDef<T>(val name: String, val type: PreferenceType<T>, val defaultValue: T?)
