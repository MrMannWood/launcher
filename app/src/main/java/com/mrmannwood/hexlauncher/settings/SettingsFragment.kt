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
import androidx.fragment.app.activityViewModels
import androidx.preference.*
import com.mrmannwood.hexlauncher.applist.AppListActivity
import com.mrmannwood.hexlauncher.applist.AppListActivity.Companion.onAppListResult
import com.mrmannwood.hexlauncher.permissions.PermissionsHelper
import com.mrmannwood.hexlauncher.permissions.PermissionsLiveData
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.launcher.R

class SettingsFragment : PreferenceFragmentCompat() {

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
                    Toast.makeText(requireContext(), R.string.no_wallpaper_app_selected, Toast.LENGTH_LONG).show()
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

    private lateinit var wallpaperAppPreference: CheckBoxPreference
    private lateinit var wallpaperPreference: CheckBoxPreference
    private lateinit var contactsPreference: CheckBoxPreference

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
                wallpaperAppPreference.isChecked = true
                wallpaperPreference.isVisible = true
            } ?: run {
                wallpaperAppPreference.summary = ""
                wallpaperAppPreference.isChecked = false
                wallpaperPreference.isVisible = false
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

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_wallpaper)
            addPreference(CheckBoxPreference(activity).apply {
                wallpaperAppPreference = this
                setTitle(R.string.preferences_wallpaper_choose_app)
                setOnPreferenceClickListener {
                    wallpaperActivityResultContracts.launch(Intent(activity, AppListActivity::class.java))
                    true
                }
            })
            addPreference(CheckBoxPreference(activity).apply {
                wallpaperPreference = this
                setTitle(R.string.preferences_wallpaper_choose_image)
                isChecked = true
                isVisible = false
                setOnPreferenceClickListener {
                    isChecked = true
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
            addPreference(CheckBoxPreference(activity).apply {
                setTitle(R.string.preferences_category_home_show_date)
                key = PreferenceKeys.Home.SHOW_DATE
            })
            addPreference(CheckBoxPreference(activity).apply {
                setTitle(R.string.preferences_category_home_show_time)
                key = PreferenceKeys.Home.SHOW_TIME
            })
        }

        if (BuildConfig.DEBUG) {
            // TODO contacts don't work yet
            val contactsCategory = PreferenceCategory(activity).apply {
                setTitle(R.string.preferences_category_contacts)
                screen.addPreference(this)
            }
            contactsCategory.apply {
                addPreference(CheckBoxPreference(activity).apply {
                    contactsPreference = this
                    setTitle(R.string.preferences_contacts_allow_search)
                    key = PreferenceKeys.Contacts.ALLOW_CONTACT_SEARCH
                })
            }
        }
        preferenceScreen = screen
    }
}