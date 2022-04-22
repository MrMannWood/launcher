package com.mrmannwood.hexlauncher.timber

fun toPriorityString(priority: Int): String {
    return when (priority) {
        android.util.Log.VERBOSE -> "V"
        android.util.Log.DEBUG -> "D"
        android.util.Log.INFO -> "I"
        android.util.Log.WARN -> "W"
        android.util.Log.ERROR -> "E"
        android.util.Log.ASSERT -> "A"
        else -> "?"
    }
}

fun isAtLeastWarn(priority: Int): Boolean {
    return priority >= android.util.Log.WARN
}
