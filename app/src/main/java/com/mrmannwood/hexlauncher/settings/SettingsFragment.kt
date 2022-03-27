package com.mrmannwood.hexlauncher.settings

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.role.RoleManagerCompat
import androidx.fragment.app.FragmentActivity
import androidx.preference.*
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.LauncherApplication
import com.mrmannwood.hexlauncher.allapps.AllAppsListFragment
import com.mrmannwood.hexlauncher.applist.AppListUpdater
import com.mrmannwood.hexlauncher.executors.diskExecutor
import com.mrmannwood.hexlauncher.role.RoleManagerHelper
import com.mrmannwood.hexlauncher.role.RoleManagerHelper.RoleManagerResult.*
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.launcher.R

class SettingsFragment : PreferenceFragmentCompat() {

    private val setHomeLauncherResultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { updateHomeRolePreference(requireActivity()) }

    private lateinit var homeRolePreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val activity = requireActivity()

        val screen = preferenceManager.createPreferenceScreen(activity)

        Preference(activity).apply {
            screen.addPreference(this)
            homeRolePreference = this
        }

        updateHomeRolePreference(activity)

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_preferences)
            addPreference(SwitchPreference(activity).apply {
                setTitle(R.string.preferences_preferences_left_handed)
                key = PreferenceKeys.User.LEFT_HANDED
                setDefaultValue(false)
            })
            addPreference(ListPreference(activity).apply {
                setTitle(R.string.preferences_home_orientation)
                key = PreferenceKeys.Home.ORIENTATION
                setDefaultValue(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT.toString())
                setEntries(R.array.preferences_home_orientation_options)
                entryValues = arrayOf(
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED.toString(),
                    ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT.toString(),
                    ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE.toString())
            })
        }

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_app_list)
            addPreference(Preference(activity).apply {
                setTitle(R.string.preferences_app_list_show_all_apps)
                setOnPreferenceClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.settings_root, AllAppsListFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
            })
            addPreference(SwitchPreference(activity).apply {
                setTitle(R.string.preferences_app_list_enable_category_search)
                key = PreferenceKeys.Apps.ENABLE_CATEGORY_SEARCH
                setDefaultValue(true)
            })
        }

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_gestures)
            addPreference(SeekBarPreference(activity).apply {
                setTitle(R.string.preferences_gestures_opacity)
                key = PreferenceKeys.Gestures.OPACITY
                setDefaultValue(100)
                min = 0
                max = 100
                seekBarIncrement = 10
            })
            addPreference(Preference(activity).apply {
                setTitle(R.string.preferences_gestures_reset_disabled)
                setOnPreferenceClickListener {
                    PreferencesRepository.getPrefs(requireContext()) { prefs -> prefs.edit {
                        listOf(
                            PreferenceKeys.Gestures.SwipeNorthWest.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeNorth.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeNorthEast.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeWest.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeEast.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeSouthWest.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeSouth.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeSouthEast.PACKAGE_NAME,
                        ).forEach { key ->
                            if (prefs.getString(key, null) == PreferenceKeys.Gestures.GESTURE_UNWANTED) {
                                remove(key)
                            }
                        }

                    } }
                    true
                }
            })
            addPreference(Preference(activity).apply {
                setTitle(R.string.preferences_gestures_disable_all)
                setOnPreferenceClickListener {
                    PreferencesRepository.getPrefs(requireContext()) { prefs -> prefs.edit {
                        listOf(
                            PreferenceKeys.Gestures.SwipeNorthWest.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeNorthEast.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeWest.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeEast.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeSouthWest.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeSouth.PACKAGE_NAME,
                            PreferenceKeys.Gestures.SwipeSouthEast.PACKAGE_NAME,
                        ).forEach { key ->
                            putString(key, PreferenceKeys.Gestures.GESTURE_UNWANTED)
                        }
                        putInt(PreferenceKeys.Gestures.OPACITY, 0)
                    } }
                    true
                }
            })
        }

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_debugging)
            addPreference(Preference(activity).apply {
                setTitle(R.string.preferences_debugging_report_a_problem)
                setOnPreferenceClickListener {
                    (activity.application as LauncherApplication).rageShakeThing(activity)
                    true
                }
            })
            addPreference(SwitchPreference(activity).apply {
                setTitle(R.string.preferences_debugging_logging_enable)
                key = PreferenceKeys.Logging.ENABLE_DISK_LOGGING
            })
        }

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_legal)
            addPreference(Preference(activity).apply {
                setTitle(R.string.preferences_privacy_policy)
                setOnPreferenceClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://mrmannwood.github.io/launcher/")))
                    true
                }
            })
        }

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_version_info)
            addPreference(Preference(activity).apply {
                title = getString(R.string.preferences_version_name, getString(R.string.app_version))
            })
            addPreference(Preference(activity).apply {
                title = getString(R.string.preferences_version_build_type, BuildConfig.BUILD_TYPE)
            })
        }

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_experimental)
            setSummary(R.string.preferences_category_experimental_summary)

            addPreference(Preference(activity).apply {
                setTitle(R.string.preferences_refresh_app_cache_title)
                setOnPreferenceClickListener {
                    val appContext = activity.applicationContext
                    diskExecutor.execute {
                        DB.get(requireContext()).appDataDao().zeroAllLastUpdateTimeStamps()
                        AppListUpdater.updateAppList(appContext)
                    }
                    true
                }
            })
        }

        preferenceScreen = screen
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(
            ResourcesCompat.getDrawable(resources, R.drawable.preference_divider, requireActivity().theme)
        )
    }

    private fun updateHomeRolePreference(activity: FragmentActivity) {
        when (RoleManagerHelper.INSTANCE.getRoleStatus(activity, RoleManagerCompat.ROLE_HOME)) {
            ROLE_HELD -> { homeRolePreference.isVisible = false }
            ROLE_NOT_AVAILABLE -> { homeRolePreference.isVisible = false }
            ROLE_NOT_HELD -> {
                homeRolePreference.apply {
                    setTitle(R.string.preferences_role_set_home)
                    setOnPreferenceClickListener {
                        val (intent, func) = RoleManagerHelper.INSTANCE.getRoleSetIntent(activity, RoleManagerCompat.ROLE_HOME)
                        setHomeLauncherResultContract.launch(intent)
                        func()
                        true
                    }
                }
            }
        }
    }
}