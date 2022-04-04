package com.mrmannwood.hexlauncher.settings

object PreferenceKeys {
    object Home {
        object Widgets {
            const val DATE = "home_widget_date"
            const val TIME = "home_widget_time"
            object Position {
                fun key(widget: String) = "${widget}_position"
            }
            object Color {
                fun key(widget: String) = "${widget}_color"
            }
        }
        const val ORIENTATION = "orientation"
    }
    object Gestures {
        val GESTURE_UNWANTED = "unwanted"
        val OPACITY = "gestures_opacity"
        object SwipeNorthEast {
            const val PACKAGE_NAME = "gestures_swipe_north_east_package_name"
        }
        object SwipeNorth {
            const val PACKAGE_NAME = "gestures_swipe_north_package_name"
        }
        object SwipeNorthWest {
            const val PACKAGE_NAME = "gestures_swipe_north_west_package_name"
        }
        object SwipeEast {
            const val PACKAGE_NAME = "gestures_swipe_right_package_name"
        }
        object SwipeWest {
            const val PACKAGE_NAME = "gestures_swipe_left_package_name"
        }
        object SwipeSouthEast {
            const val PACKAGE_NAME = "gestures_swipe_south_east_package_name"
        }
        object SwipeSouth {
            const val PACKAGE_NAME = "gestures_swipe_south_package_name"
        }
        object SwipeSouthWest {
            const val PACKAGE_NAME = "gestures_swipe_south_west_package_name"
        }
    }
    object Apps {
        const val ENABLE_CATEGORY_SEARCH = "enable_category_search"
        const val ENABLE_ALL_APPS_HOT_KEY = "enable_all_apps_hotkey"
    }
    object User {
        const val LEFT_HANDED = "left_handed"
    }
    object Logging {
        const val ENABLE_DISK_LOGGING = "enable_disk_logging"
    }
    object Version {
        const val LAST_RUN_VERSION_NAME = "previous_version_name"
    }
}