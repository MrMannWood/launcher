package com.mrmannwood.hexlauncher.launcher

import android.graphics.drawable.Drawable
import com.mrmannwood.hexlauncher.executors.InlineExecutor
import java.util.*
import java.util.concurrent.Executor

enum class SearchTermType {
    FullName,
    Label,
    Tag,
    Category,
}

data class AppInfo(
    val packageName: String,
    val icon: Provider<Drawable>,
    val backgroundColor: Int,
    val label: String,
    val hidden: Boolean,
    val backgroundHidden: Boolean,
    val categories: List<String>,
    val tags: List<String>
) {
    val lowerLabel = label.lowercase(Locale.ROOT)
    val searchTerms : Map<String, SearchTermType> = (
            categories.map { it to SearchTermType.Category } +
                    tags.map { it to SearchTermType.Tag } +
                    lowerLabel.split(' ').map { it to SearchTermType.Label }
            ).toMap()
}

class Provider<T>(
    private val init: () -> T,
    private val executor: Executor = InlineExecutor
) {

    init {
        if (executor != InlineExecutor) {
            executor.execute { get() }
        }
    }

    private var t: T? = null

    fun get(): T {
        var value = t
        if (value == null) {
            synchronized(this) {
                value = t
                if (value == null) {
                    value = init()
                    t = value
                }
            }
        }
        return value!!
    }

    fun get(callback: (T) -> Unit) {
        t?.let {
            callback(it)
        } ?: run {
            executor.execute { callback(get()) }
        }
    }
}