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
        object Slots {
            const val SLOT_1 = "home_slot_1"
            const val SLOT_2 = "home_slot_1"
            const val SLOT_3 = "home_slot_1"
            const val SLOT_4 = "home_slot_1"
            const val SLOT_5 = "home_slot_1"
            const val SLOT_6 = "home_slot_1"
            const val SLOT_7 = "home_slot_1"
            const val SLOT_8 = "home_slot_1"

            val all = listOf(SLOT_1, SLOT_2, SLOT_3, SLOT_4, SLOT_5, SLOT_6, SLOT_7, SLOT_8)
        }
        object Widgets {
            const val DATE = "date"
            const val TIME = "time"

            val all = listOf(DATE, TIME)
        }
    }
    object AppList {
        const val SHOW_ALL_APPS = "app_list_show_all"
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
    object Version {
        const val LAST_RUN_VERSION_NAME = "previous_version_name"
    }
}