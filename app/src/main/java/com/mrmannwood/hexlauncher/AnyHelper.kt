package com.mrmannwood.hexlauncher

fun <T : Any> T.init(init: T.() -> Unit) : T {
    init()
    return this
}