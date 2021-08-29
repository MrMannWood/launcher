package com.mrmannwood.preferences

data class PreferenceValue<T>(val def: PreferenceDef<T>, val value: T?)