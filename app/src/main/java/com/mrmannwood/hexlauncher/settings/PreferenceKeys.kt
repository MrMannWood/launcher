package com.mrmannwood.hexlauncher.settings

object PreferenceKeys {
    object Wallpaper {
        const val APP_NAME = "wallpaper_app_name"
        const val PACKAGE_NAME = "wallpaper_app_package"
    }
    object Contacts {
        const val ALLOW_CONTACT_SEARCH = "contacts_allow_search"
    }
    object Home {
        const val SHOW_DATE = "home_show_date"
        const val SHOW_TIME = "home_show_time"
    }
    object Gestures {
        object SwipeRight {
            const val APP_NAME = "gestures_swipe_right_app_name"
            const val PACKAGE_NAME = "gestures_swipe_right_package_name"
        }
        object SwipeLeft {
            const val APP_NAME = "gestures_swipe_left_app_name"
            const val PACKAGE_NAME = "gestures_swipe_left_package_name"
        }
    }
    object AutoUpdate {
        const val ALLOW_AUTO_UPDATE = "update_allow_auto"
    }
}