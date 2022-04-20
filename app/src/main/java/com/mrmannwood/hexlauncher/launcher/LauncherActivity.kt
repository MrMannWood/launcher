package com.mrmannwood.hexlauncher.launcher

import android.app.SearchManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import com.mrmannwood.applist.AppListManager
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.TestLabUtil.isTestLab
import com.mrmannwood.hexlauncher.activity.BaseActivity
import com.mrmannwood.hexlauncher.appcustomize.AppCustomizationFragment
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.hexlauncher.executors.OriginalThreadCallback
import com.mrmannwood.hexlauncher.home.HomeFragment
import com.mrmannwood.hexlauncher.isVersionStringLess
import com.mrmannwood.hexlauncher.nux.NUXHostFragment
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository
import com.mrmannwood.hexlauncher.settings.PreferencesRepository.watchPref
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.launcher.R
import timber.log.Timber
import java.lang.Exception

class LauncherActivity : BaseActivity(), AppListFragment.AppListHostActivity {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goFullscreen()
        setContentView(R.layout.activity_launcher)
        supportActionBar?.hide()

        if (supportFragmentManager.findFragmentById(R.id.container) == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, HomeFragment())
                .addToBackStack("HomeFragment")
                .commit()
        }

        PreferencesRepository.getPrefs(
            this,
            OriginalThreadCallback.create { prefs ->
                if (checkShouldShowNux(prefs)) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, NUXHostFragment())
                        .commit()
                }
            })
        watchPref(
            context = applicationContext,
            key = PreferenceKeys.Home.ORIENTATION,
            extractor = PreferenceExtractor.StringExtractor
        ).observe(this) { orientation ->
            requestedOrientation = orientation?.toIntOrNull() ?: ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
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
        if (isTestLab(this)) return false
        val showNux = prefs.getString(PreferenceKeys.Version.LAST_RUN_VERSION_NAME, null)?.let {
            isVersionStringLess(it, "1.4.1")
        } ?: run { true }
        prefs.edit {
            putString(PreferenceKeys.Version.LAST_RUN_VERSION_NAME, BuildConfig.VERSION_NAME)
        }
        return showNux
    }

    private fun goFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }

    private val appListFragmentHost = object : AppListFragment.Host<Void>(
        killFragment = { supportFragmentManager.popBackStack("HomeFragment", 0) }
    ) {
        override fun onSearchButtonPressed(searchTerm: String) {
            startActivity(Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, searchTerm)
            })
            end()
        }

        override fun onAppSelected(appInfo: AppInfo) {
            if (!isTestLab(this@LauncherActivity)) {
                try {
                    startActivity(Intent().apply { component = appInfo.componentName }
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (e: Exception) {
                    Toast.makeText(this@LauncherActivity, R.string.unable_to_start_app, Toast.LENGTH_LONG).show()
                }
            }
            end()
        }

        override fun onAppInfoBinding(view: View, appInfo: AppInfo) {
            view.setOnCreateContextMenuListener { menu, _, _ ->
                menu.setHeaderTitle(appInfo.label)
                menu.add(R.string.menu_item_app_details).setOnMenuItemClickListener {
                    val rect = Rect()
                    view.getGlobalVisibleRect(rect)
                    AppListManager.startAppDetailsActivity(
                        this@LauncherActivity,
                        appInfo.launcherItem,
                        rect
                    )
                    end()
                    true
                }
                menu.add(R.string.menu_item_app_customize).setOnMenuItemClickListener {
                    end()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, AppCustomizationFragment.forComponent(appInfo.componentName))
                        .addToBackStack("AppCustomizationFragment")
                        .commit()
                    true
                }
                menu.add(R.string.menu_item_uninstall_app_title).setOnMenuItemClickListener {
                    end()
                    startActivity(Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${appInfo.componentName.packageName}")
                    })
                    true
                }
            }
        }
    }
}