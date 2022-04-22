package com.mrmannwood.hexlauncher.timber

import java.util.*

data class Log(
    val date: Date,
    val priority: Int,
    val tag: String?,
    val message: String,
    val t: Throwable?
)
