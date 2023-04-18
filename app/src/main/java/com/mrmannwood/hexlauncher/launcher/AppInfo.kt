package com.mrmannwood.hexlauncher.launcher

import android.graphics.drawable.Drawable
import com.mrmannwood.applist.LauncherItem
import com.mrmannwood.hexlauncher.executors.InlineExecutor
import com.mrmannwood.hexlauncher.removeChars
import java.util.*
import java.util.concurrent.Executor

enum class SearchTermType {
    FullName,
    Label,
    Tag,
    Category,
}

interface HexItem {
    val label: String
    val icon: Provider<Drawable>
    val hidden: Boolean
    val backgroundColor: Int
    val backgroundHidden: Boolean
}

data class AppInfo(
    val launcherItem: LauncherItem,
    override val backgroundColor: Int,
    override val hidden: Boolean,
    override val backgroundHidden: Boolean,
    val categories: List<String>,
    val tags: List<String>
) : HexItem {
    val componentName = launcherItem.componentName
    override val label = launcherItem.label
    override val icon = Provider({ launcherItem.icon })
    val lowerLabel = label.lowercase(Locale.ROOT)
    val searchTerms: Map<String, SearchTermType> = (
            categories.map { it to SearchTermType.Category } +
                    tags.map { it to SearchTermType.Tag } +
                    listOf(
                        lowerLabel.removeChars(
                            charArrayOf(
                                ' ',
                                '-',
                                '_'
                            )
                        ) to SearchTermType.Label
                    ) +
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
