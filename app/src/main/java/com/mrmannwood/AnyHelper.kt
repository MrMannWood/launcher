package com.mrmannwood

fun <T : Any> T.init(init: T.() -> Unit) : T {
    init()
    return this
}