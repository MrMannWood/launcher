package com.mrmannwood.preferences

sealed class PreferenceType<T>(val type: Class<T>) {
    object STRING: PreferenceType<String>(String::class.java)
    object BOOLEAN: PreferenceType<Boolean>(Boolean::class.java)
    object INT: PreferenceType<Int>(Int::class.java)
    object FLOAT: PreferenceType<Float>(Float::class.java)
    object DOUBLE: PreferenceType<Double>(Double::class.java)
    object LONG: PreferenceType<Long>(Long::class.java)

    override fun toString(): String = type.toString()
}
