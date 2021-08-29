package com.mrmannwood.hexlauncher.settings

import android.graphics.Color
import com.mrmannwood.preferences.PreferenceDef
import com.mrmannwood.preferences.PreferenceType.BOOLEAN
import com.mrmannwood.preferences.PreferenceType.FLOAT
import com.mrmannwood.preferences.PreferenceType.INT
import com.mrmannwood.preferences.PreferenceType.STRING

object PreferenceKeys {

    val allPreferences = mutableListOf<PreferenceDef<*>>()

    object Home {
        sealed class Widget {
            abstract val POSITION: PreferenceDef<Float>
            abstract val COLOR: PreferenceDef<Int>
            object DATE: Widget() {
                override val POSITION = PreferenceDef(name = "home_widget_date_position", type = FLOAT, defaultValue = 0F).apply {
                    allPreferences.add(this)
                }
                override val COLOR = PreferenceDef(name = "home_widget_date_color", type = INT, defaultValue = Color.WHITE).apply {
                    allPreferences.add(this)
                }
            }
            object TIME: Widget() {
                override val POSITION = PreferenceDef(name = "home_widget_time_position", type = FLOAT, defaultValue = 0F).apply {
                    allPreferences.add(this)
                }
                override val COLOR = PreferenceDef(name = "home_widget_time_color", type = INT, defaultValue = Color.WHITE).apply {
                    allPreferences.add(this)
                }
            }
        }
    }
    sealed class Gesture {
        abstract val APP_NAME: PreferenceDef<String>
        abstract val PACKAGE_NAME: PreferenceDef<String>
        object SwipeRight: Gesture() {
            override val APP_NAME = PreferenceDef(name = "gestures_swipe_right_app_name", type = STRING, defaultValue = null).apply {
                allPreferences.add(this)
            }
            override val PACKAGE_NAME = PreferenceDef(name = "gestures_swipe_right_package_name", type = STRING, defaultValue = null).apply {
                allPreferences.add(this)
            }
        }
        object SwipeLeft: Gesture() {
            override val APP_NAME = PreferenceDef(name = "gestures_swipe_left_app_name", type = STRING, defaultValue = null).apply {
                allPreferences.add(this)
            }
            override val PACKAGE_NAME = PreferenceDef(name = "gestures_swipe_left_package_name", type = STRING, defaultValue = null).apply {
                allPreferences.add(this)
            }
        }
    }
    object Apps {
        val USE_HEX_GRID = PreferenceDef(name = "apps_use_hex_grid", type = BOOLEAN, defaultValue = false).apply {
            allPreferences.add(this)
        }
    }
    object Font {
        val USE_ATKINSON_HYPERLEGIBLE = PreferenceDef(name = "use_atkison_hyperlegible", type = BOOLEAN, defaultValue = true).apply {
            allPreferences.add(this)
        }
    }
    object Logging {
        val ENABLE_DISK_LOGGING = PreferenceDef(name = "enable_disk_logging", type = BOOLEAN, defaultValue = false).apply {
            allPreferences.add(this)
        }
    }
    object Version {
        val LAST_RUN_VERSION_NAME = PreferenceDef(name = "previous_version_name", type = STRING, defaultValue = null).apply {
            allPreferences.add(this)
        }
    }
}