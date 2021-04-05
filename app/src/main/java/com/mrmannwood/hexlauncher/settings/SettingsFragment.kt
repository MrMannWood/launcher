package com.mrmannwood.hexlauncher.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.role.RoleManagerCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.preference.*
import com.mrmannwood.hexlauncher.applist.AppListActivity
import com.mrmannwood.hexlauncher.applist.AppListActivity.Companion.onAppListResult
import com.mrmannwood.hexlauncher.legal.PrivacyPolicyActivity
import com.mrmannwood.hexlauncher.permissions.PermissionsLiveData
import com.mrmannwood.hexlauncher.role.RoleManagerHelper
import com.mrmannwood.hexlauncher.role.RoleManagerHelper.RoleManagerResult.ROLE_HELD
import com.mrmannwood.hexlauncher.role.RoleManagerHelper.RoleManagerResult.ROLE_NOT_AVAILABLE
import com.mrmannwood.hexlauncher.role.RoleManagerHelper.RoleManagerResult.ROLE_NOT_HELD
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.launcher.R

class SettingsFragment : PreferenceFragmentCompat() {

    private val setHomeLauncherResultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ -> updateHomeRolePreference(requireActivity()) }

    private val wallpaperActivityResultContracts = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data.onAppListResult(
                onSuccess = { appName, packageName ->
                    prefs.edit {
                        putString(PreferenceKeys.Wallpaper.APP_NAME, appName)
                        putString(PreferenceKeys.Wallpaper.PACKAGE_NAME, packageName)
                    }
                },
                onFailure = {
                    Toast.makeText(requireContext(), R.string.no_app_selected, Toast.LENGTH_LONG).show()
                }
        )
    }

    private val preferenceSwipeRightResultContract = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data.onAppListResult(
                onSuccess = { appName, packageName ->
                    prefs.edit {
                        putString(PreferenceKeys.Gestures.SwipeRight.APP_NAME, appName)
                        putString(PreferenceKeys.Gestures.SwipeRight.PACKAGE_NAME, packageName)
                    }
                },
                onFailure = {
                    Toast.makeText(requireContext(), R.string.no_app_selected, Toast.LENGTH_LONG).show()
                }
        )
    }

    private val preferenceSwipeLeftResultContract = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data.onAppListResult(
                onSuccess = { appName, packageName ->
                    prefs.edit {
                        putString(PreferenceKeys.Gestures.SwipeLeft.APP_NAME, appName)
                        putString(PreferenceKeys.Gestures.SwipeLeft.PACKAGE_NAME, packageName)
                    }
                },
                onFailure = {
                    Toast.makeText(requireContext(), R.string.no_app_selected, Toast.LENGTH_LONG).show()
                }
        )
    }

    private val requestContactsPermissionContract = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        prefs.edit {
            putBoolean(PreferenceKeys.Contacts.ALLOW_CONTACT_SEARCH, isGranted)
        }
    }

    private val settingsViewModel : SettingsViewModel by activityViewModels()
    private lateinit var prefs : SharedPreferences

    private lateinit var homeRolePreference: Preference
    private lateinit var wallpaperAppPreference: Preference
    private lateinit var wallpaperPreference: Preference
    private lateinit var swipeRightAppPreference: Preference
    private lateinit var swipeLeftAppPreference: Preference

    private var wallpaperAppPackageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsViewModel.preferencesLiveData.observe(this) { sp -> prefs = sp }
        settingsViewModel.wallpaperPackageLiveData.observe(this) { app ->
            wallpaperAppPackageName = app
        }
        settingsViewModel.wallpaperAppNameLiveData.observe(this) {
            it?.let { appName ->
                wallpaperAppPreference.summary = appName
                wallpaperPreference.isVisible = true
            } ?: run {
                wallpaperAppPreference.summary = ""
                wallpaperPreference.isVisible = false
            }
        }
        settingsViewModel.swipeRightLiveData.observe(this) { appName ->
            if (appName != null) {
                swipeRightAppPreference.summary = appName
            } else {
                swipeRightAppPreference.summary = ""
            }
        }
        settingsViewModel.swipeLeftLiveData.observe(this) { appName ->
            if (appName != null) {
                swipeLeftAppPreference.summary = appName
            } else {
                swipeLeftAppPreference.summary = ""
            }
        }
        settingsViewModel.contactsPermissionLiveData.observe(this) { permissionsResult ->
            when (permissionsResult) {
                is PermissionsLiveData.PermissionsResult.PrefGrantedPermissionGranted -> { /* checked */  }
                is PermissionsLiveData.PermissionsResult.PrefGrantedPermissionDenied -> {
                    requestContactsPermissionContract.launch(
                            settingsViewModel.contactsPermissionLiveData.permission)
                }
                is PermissionsLiveData.PermissionsResult.PrefDeniedPermissionGranted -> {
                    AlertDialog.Builder(requireActivity())
                            .setTitle(R.string.preferences_contacts_dialog_title)
                            .setMessage(R.string.preferences_contacts_dialog_message)
                            .setPositiveButton(R.string.preferences_contacts_dialog_button_positive) { _, _ ->
                                startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", requireActivity().packageName, null)
                                        }
                                )
                            }
                            .setNegativeButton(R.string.preferences_contacts_dialog_button_negative) { _, _ -> }
                            .show()
                }
                is PermissionsLiveData.PermissionsResult.PrefDeniedPermissionDenied -> { /* unchecked */ }
            }
        }
    }

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
            setTitle(R.string.preferences_category_wallpaper)
            addPreference(Preference(activity).apply {
                wallpaperAppPreference = this
                setTitle(R.string.preferences_wallpaper_choose_app)
                setOnPreferenceClickListener {
                    wallpaperActivityResultContracts.launch(Intent(activity, AppListActivity::class.java))
                    true
                }
            })
            addPreference(Preference(activity).apply {
                wallpaperPreference = this
                setTitle(R.string.preferences_wallpaper_choose_image)
                isVisible = false
                setOnPreferenceClickListener {
                    wallpaperAppPackageName?.let { app ->
                        startActivity(activity.packageManager.getLaunchIntentForPackage(app))
                    }
                    true
                }
            })
        }

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_home)
            addPreference(SwitchPreference(activity).apply {
                setTitle(R.string.preferences_category_home_show_date)
                key = PreferenceKeys.Home.SHOW_DATE
            })
            addPreference(SwitchPreference(activity).apply {
                setTitle(R.string.preferences_category_home_show_time)
                key = PreferenceKeys.Home.SHOW_TIME
            })
        }

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_gestures)
            addPreference(Preference(activity).apply {
                swipeRightAppPreference = this
                setTitle(R.string.preferences_gestures_swipe_right)
                setOnPreferenceClickListener {
                    preferenceSwipeRightResultContract.launch(Intent(activity, AppListActivity::class.java))
                    true
                }
            })
            addPreference(Preference(activity).apply {
                swipeLeftAppPreference = this
                setTitle(R.string.preferences_gestures_swipe_left)
                setOnPreferenceClickListener {
                    preferenceSwipeLeftResultContract.launch(Intent(activity, AppListActivity::class.java))
                    true
                }
            })
        }

        if (BuildConfig.DEBUG) {
            // TODO contacts don't work yet
            val contactsCategory = PreferenceCategory(activity).apply {
                setTitle(R.string.preferences_category_contacts)
                screen.addPreference(this)
            }
            contactsCategory.apply {
                addPreference(SwitchPreference(activity).apply {
                    setTitle(R.string.preferences_contacts_allow_search)
                    key = PreferenceKeys.Contacts.ALLOW_CONTACT_SEARCH
                })
            }
        }
        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_legal)
            addPreference(Preference(activity).apply {
                setTitle(R.string.preferences_privacy_policy)
                setOnPreferenceClickListener {
                    startActivity(Intent(activity, PrivacyPolicyActivity::class.java))
                    true
                }
            })
        }

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_version_info)
            addPreference(SwitchPreference(activity).apply {
                setTitle(R.string.preferences_allow_auto_update_check)
                key = PreferenceKeys.AutoUpdate.ALLOW_AUTO_UPDATE
            })
            addPreference(Preference(activity).apply {
                title = getString(R.string.preferences_version_name, getString(R.string.app_version))
            })
            addPreference(Preference(activity).apply {
                title = getString(R.string.preferences_version_build_type, BuildConfig.BUILD_TYPE)
            })
        }

        preferenceScreen = screen
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