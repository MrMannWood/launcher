package com.mrmannwood.hexlauncher.settings

object PreferenceKeys {
    object Contacts {
        const val ALLOW_CONTACT_SEARCH = "contacts_allow_search"
    }
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
    object Apps {
        const val USE_APP_DATABASE = "use_app_database"
    }
    object Experimental {
        const val SHOW_EXPERIMENTAL = "show_experimental"
    }
    object Version {
        const val LAST_RUN_VERSION_NAME = "previous_version_name"
    }
}