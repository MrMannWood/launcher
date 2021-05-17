package com.mrmannwood.hexlauncher.launcher

import android.app.SearchManager
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.Result
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.hexlauncher.contacts.ContactData
import com.mrmannwood.hexlauncher.home.HomeFragment
import com.mrmannwood.hexlauncher.nux.NUXActivity
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.launcher.R

class LauncherActivity : AppCompatActivity(), AppListFragment.AppListHostActivity {

    private val viewModel by viewModels<LauncherActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        supportActionBar?.hide()

        viewModel.preferencesLiveData.observe(this) { prefs ->
            if (checkShouldShowNux(prefs)) {
                startActivity(Intent(this@LauncherActivity, NUXActivity::class.java))
            }
        }
        viewModel.appInfoLiveData.observe(this) { }

        if (supportFragmentManager.findFragmentById(R.id.container) == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, HomeFragment())
                .commit()
        }
    }

    override fun onBackPressed() {
        val handled = supportFragmentManager.findFragmentById(R.id.container)?.let { fragment ->
            if (fragment is HandleBackPressed) {
                fragment.handleBackPressed()
            } else {
                false
            }
        } ?: false
        if (!handled) {
            super.onBackPressed()
        }
    }

    override fun getAppListHost() : AppListFragment.Host<*> {
        return appListFragmentHost
    }

    private fun checkShouldShowNux(prefs: SharedPreferences) : Boolean {
        return prefs.getString(PreferenceKeys.Version.LAST_RUN_VERSION_NAME, null)?.let { _ ->
            // todo make this smarter, so new nuxes can be shown as necessary
            false
        } ?: run {
            prefs.edit {
                putString(PreferenceKeys.Version.LAST_RUN_VERSION_NAME, BuildConfig.VERSION_NAME)
            }
            true
        }
    }

    private val appListFragmentHost = object : AppListFragment.Host<Void>(
        killFragment = { _ -> supportFragmentManager.popBackStack() }
    ) {

        override fun showContacts(): Boolean = true

        override fun onContactClicked(contact: ContactData) {
            startActivity(Intent(Intent.ACTION_VIEW, contact.uri))
            end()
        }

        override fun onSearchButtonPressed(searchTerm: String) {
            startActivity(Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, searchTerm)
            })
            end()
        }

        override fun onAppSelected(appInfo: AppInfo) {
            packageManager.getLaunchIntentForPackage(appInfo.packageName)?.let { intent ->
                startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                end()
            } ?: run {
                Toast.makeText(this@LauncherActivity, R.string.unable_to_start_app, Toast.LENGTH_LONG).show()
            }
        }

        override fun onAppInfoBinding(view: View, appInfo: AppInfo) {
            view.setOnCreateContextMenuListener { menu, _, _ ->
                menu.setHeaderTitle(appInfo.label)
                menu.add(R.string.menu_item_app_details).setOnMenuItemClickListener {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${appInfo.packageName}")
                    })
                    true
                }
                menu.add(R.string.menu_item_uninstall_app_title).setOnMenuItemClickListener {
                    startActivity(Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${appInfo.packageName}")
                    })
                    true
                }
            }
        }
    }
}