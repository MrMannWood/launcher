package com.mrmannwood.iconpack

import android.content.ComponentName
import android.graphics.drawable.Drawable

class IconPackIconInfo(
    val componentName: ComponentName,
    val drawableName: String,
    val drawableProvider: () -> Drawable?
)

private val fullComponentInfoRegex = Regex("""ComponentInfo\{([^\/]+)\/([^}]+)\}""")

fun parseComponentName(componentString: String): ComponentName? {
    val match = fullComponentInfoRegex.find(componentString)
    if (match != null) return ComponentName(match.groupValues[1], match.groupValues[2])
    return null
}
