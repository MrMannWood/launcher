package com.mrmannwood.hexlauncher.iconpack

import android.graphics.drawable.Drawable
import com.mrmannwood.hexlauncher.launcher.Provider

class IconPackIconInfo(
    val component: Component,
    val drawableName: String,
    val drawableProvider: Provider<Drawable?>
)

data class Component(
    val packageName: String,
    val activityName: String
) {
    companion object {
        private val regex = Regex("""ComponentInfo\{([a-zA-Z\.0-9_]+)\/([a-zA-Z\.0-9_]+)\}""")
        fun parse(componentString: String): Component? {
            val match = regex.find(componentString) ?: return null
            return Component(
                match.groupValues[1],
                match.groupValues[2]
            )
        }
    }
}