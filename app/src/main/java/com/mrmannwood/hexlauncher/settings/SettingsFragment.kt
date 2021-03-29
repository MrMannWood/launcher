package com.mrmannwood.hexlauncher.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import com.mrmannwood.hexlauncher.applist.AppListActivity
import com.mrmannwood.hexlauncher.applist.AppListActivity.Companion.onAppListResult
import com.mrmannwood.hexlauncher.contacts.ContactsLoader
import com.mrmannwood.hexlauncher.permissions.PermissionsHelper
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.launcher.R

class SettingsFragment : PreferenceFragmentCompat() {

    private val onWallpaperPackageChangedCallbacks = mutableListOf<() -> Unit>()

    private val prefs by lazy {
        Preferences.getPrefs(requireContext())
    }

    private val wallpaperActivityResultContracts = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data.onAppListResult(
                onSuccess = { appName, packageName ->
                    Preferences.getPrefs(requireContext()).apply {
                        putString(PreferenceKeys.Wallpaper.APP_NAME, appName)
                        putString(PreferenceKeys.Wallpaper.PACKAGE_NAME, packageName)
                    }
                },
                onFailure = {
                    Toast.makeText(requireContext(), R.string.no_wallpaper_app_selected, Toast.LENGTH_LONG).show()
                }
        )
        onWallpaperPackageChangedCallbacks.forEach { it() }
    }

    private val requestContactsPermissionContract = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Preferences.getPrefs(requireContext()).setBoolean(PreferenceKeys.Contacts.ALLOW_CONTACT_SEARCH, true)
        } else {
            Preferences.getPrefs(requireContext()).remove(PreferenceKeys.Contacts.ALLOW_CONTACT_SEARCH)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val activity = requireActivity()

        val screen = preferenceManager.createPreferenceScreen(activity)

        PreferenceCategory(activity).apply {
            screen.addPreference(this)
            setTitle(R.string.preferences_category_wallpaper)
            addPreference(CheckBoxPreference(activity).apply {
                setTitle(R.string.preferences_wallpaper_choose_app)
                val init = {
                    prefs.getString(PreferenceKeys.Wallpaper.APP_NAME)?.let {
                        summary = it
                        isChecked = true
                    } ?: run {
                        isChecked = false
                    }
                }
                init()
                setOnPreferenceClickListener {
                    wallpaperActivityResultContracts.launch(Intent(activity, AppListActivity::class.java))
                    isChecked = prefs.checkExists(PreferenceKeys.Wallpaper.APP_NAME)
                    true
                }
                onWallpaperPackageChangedCallbacks.add(init)
            })
            addPreference(CheckBoxPreference(activity).apply {
                setTitle(R.string.preferences_wallpaper_choose_image)
                val init = {
                    isChecked = true
                    isVisible = prefs.getString(PreferenceKeys.Wallpaper.PACKAGE_NAME) != null
                }
                init()
                setOnPreferenceClickListener {
                    isChecked = true
                    startActivity(
                            activity.packageManager.getLaunchIntentForPackage(prefs.getString(PreferenceKeys.Wallpaper.PACKAGE_NAME)!!)
                    )
                    true
                }
                onWallpaperPackageChangedCallbacks.add(init)
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
                    setTitle(R.string.preferences_contacts_allow_search)
                    isChecked = prefs.getBoolean(PreferenceKeys.Contacts.ALLOW_CONTACT_SEARCH)
                    setOnPreferenceClickListener { _ ->
                        val hasPermission = PermissionsHelper.checkHasPermission(activity, ContactsLoader.CONTACTS_PERMISSION)
                        if (isChecked) {
                            if (!hasPermission) {
                                requestContactsPermissionContract.launch(ContactsLoader.CONTACTS_PERMISSION)
                            }
                        } else if (hasPermission) {
                            AlertDialog.Builder(activity)
                                    .setTitle(R.string.preferences_contacts_dialog_title)
                                    .setMessage(R.string.preferences_contacts_dialog_message)
                                    .setPositiveButton(R.string.preferences_contacts_dialog_button_positive) { _, _ ->
                                        startActivity(
                                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                    data = Uri.fromParts("package", activity.packageName, null)
                                                }
                                        )
                                    }
                                    .setNegativeButton(R.string.preferences_contacts_dialog_button_negative) { _, _ -> }
                                    .show()
                        }
                        true
                    }
                })
            }
        }
        preferenceScreen = screen
    }
}