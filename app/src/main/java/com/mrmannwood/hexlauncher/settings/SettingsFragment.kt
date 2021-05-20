package com.mrmannwood.hexlauncher.settings

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.role.RoleManagerCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mrmannwood.hexlauncher.allapps.AllAppsListFragment
import com.mrmannwood.hexlauncher.applist.AppListActivity
import com.mrmannwood.hexlauncher.applist.AppListActivity.Companion.decorateForAppListLaunch
import com.mrmannwood.hexlauncher.applist.AppListActivity.Companion.onAppListResult
import com.mrmannwood.hexlauncher.launcher.LauncherActivity
import com.mrmannwood.hexlauncher.permissions.PermissionsLiveData
import com.mrmannwood.hexlauncher.role.RoleManagerHelper
import com.mrmannwood.hexlauncher.role.RoleManagerHelper.RoleManagerResult.*
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.launcher.R

class SettingsFragment : PreferenceFragmentCompat() {

    private val setHomeLauncherResultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { updateHomeRolePreference(requireActivity()) }

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
    private val prefs = PreferencesLiveData.get().getSharedPreferences()

    private lateinit var homeRolePreference: Preference
    private lateinit var swipeRightAppPreference: Preference
    private lateinit var swipeLeftAppPreference: Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsViewModel.swipeRightLiveData.observe(this) { appName ->
            if (appName != null) {
                swipeRightAppPreference.summary = appName
            } else {
                swipeRightAppPreference.setSummary(R.string.preferences_gestures_unset)
            }
        }
        settingsViewModel.swipeLeftLiveData.observe(this) { appName ->
            if (appName != null) {
                swipeLeftAppPreference.summary = appName
            } else {
                swipeLeftAppPreference.setSummary(R.string.preferences_gestures_unset)
            }
        }
        settingsViewModel.contactsPermissionLiveData.observe(this) { permissionsResult ->
            when (permissionsResult) {
                is PermissionsLiveData.PermissionsResult.Granted -> { /* checked */  }
                is PermissionsLiveData.PermissionsResult.NotGranted -> { /* unchecked */ }
                is PermissionsLiveData.PermissionsResult.UserShouldGrantPermission -> {
                    requestContactsPermissionContract.launch(
                            settingsViewModel.contactsPermissionLiveData.permission)
                }
                is PermissionsLiveData.PermissionsResult.UserShouldRevokePermission -> {
                    MaterialAlertDialogBuilder(requireActivity())
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
        }

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_gestures)
            addPreference(Preference(activity).apply {
                swipeRightAppPreference = this
                setTitle(R.string.preferences_gestures_swipe_right)
                setOnPreferenceClickListener {
                    preferenceSwipeRightResultContract.launch(
                            Intent(activity, AppListActivity::class.java)
                                    .decorateForAppListLaunch(R.string.preferences_gestures_swipe_right_chooser_title))
                    true
                }
            })
            addPreference(Preference(activity).apply {
                swipeLeftAppPreference = this
                setTitle(R.string.preferences_gestures_swipe_left)
                setOnPreferenceClickListener {
                    preferenceSwipeLeftResultContract.launch(
                            Intent(activity, AppListActivity::class.java)
                                    .decorateForAppListLaunch(R.string.preferences_gestures_swipe_left_chooser_title))
                    true
                }
            })
        }

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_contacts)
            addPreference(SwitchPreference(activity).apply {
                setTitle(R.string.preferences_contacts_allow_search)
                key = PreferenceKeys.Contacts.ALLOW_CONTACT_SEARCH
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
            addPreference(SwitchPreference(activity).apply {
                setTitle(R.string.preferences_app_database_title)
                setSummary(R.string.preferences_app_database_description)
                key = PreferenceKeys.Apps.USE_APP_DATABASE
                val enabled = prefs.getBoolean(key, false)
                setOnPreferenceClickListener {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(R.string.preferences_app_database_dialog_title)
                        .setMessage(R.string.preferences_app_database_dialog_message)
                        .setPositiveButton(R.string.preferences_app_database_dialog_button_positive) { _, _ ->
                            forceRestart()
                        }
                        .setNegativeButton(R.string.preferences_app_database_dialog_button_negative) { _, _ ->
                            this@apply.isChecked = enabled
                            prefs.edit { putBoolean(PreferenceKeys.Apps.USE_APP_DATABASE, enabled) }
                        }
                        .show()
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

    private fun forceRestart() {
        val context = requireContext()
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).set(
            AlarmManager.RTC,
            System.currentTimeMillis() + 100,
            PendingIntent.getActivity(
                context,
                123456,
                Intent(context, LauncherActivity::class.java),
                PendingIntent.FLAG_CANCEL_CURRENT
            )
        )
        Runtime.getRuntime().exit(0)
    }
}