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
        private val activityOnlyRegex = Regex("""ComponentInfo\{([^}]+)\}""")
        fun parse(componentString: String): ComponentName? {
            var match = fullComponentInfoRegex.find(componentString)
            if (match != null) return ComponentName.unflattenFromString(componentString)
            match = activityOnlyRegex.find(componentString)
            if (match != null) return ComponentName.unflattenFromString(componentString)
            return null
        }
    }
}