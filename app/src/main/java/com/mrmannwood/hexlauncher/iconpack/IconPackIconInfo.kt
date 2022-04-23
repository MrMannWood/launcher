package com.mrmannwood.hexlauncher.iconpack

import android.content.ComponentName
import android.graphics.drawable.Drawable
import com.mrmannwood.hexlauncher.launcher.Provider

class IconPackIconInfo(
    val componentName: ComponentName,
    val drawableName: String,
    val drawableProvider: Provider<Drawable?>
)

data class Component(
    val packageName: String? = null,
    val activityName: String
) {
    companion object {
        private val fullComponentInfoRegex = Regex("""ComponentInfo\{([^\/]+)\/([^}]+)\}""")
        fun parse(componentString: String): ComponentName? {
            val match = fullComponentInfoRegex.find(componentString)
            if (match != null) return ComponentName(match.groupValues[1], match.groupValues[2])
            return null
        }
    }
}
