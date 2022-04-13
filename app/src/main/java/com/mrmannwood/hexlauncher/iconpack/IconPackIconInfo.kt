package com.mrmannwood.hexlauncher.iconpack

import android.graphics.drawable.Drawable
import com.mrmannwood.hexlauncher.launcher.Provider

class IconPackIconInfo(
    val component: Component,
    val drawableName: String,
    val drawableProvider: Provider<Drawable?>
)

data class Component(
    val packageName: String? = null,
    val activityName: String
) {
    companion object {
        private val fullComponentInfoRegex = Regex("""ComponentInfo\{([^\/]+)\/([^}]+)\}""")
        private val activityOnlyRegex = Regex("""ComponentInfo\{([^}]+)\}""")
        fun parse(componentString: String): Component? {
            var match = fullComponentInfoRegex.find(componentString)
            if (match != null) return Component(match.groupValues[1], match.groupValues[2])
            match = activityOnlyRegex.find(componentString)
            if (match != null) return Component(activityName = match.groupValues[1])
            return null
        }
    }
}