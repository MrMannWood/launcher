package com.mrmannwood.hexlauncher.launcher

import android.app.SearchManager
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.core.view.WindowCompat
import com.mrmannwood.applist.AppListManager
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.LauncherApplication
import com.mrmannwood.hexlauncher.TestLabUtil.isTestLab
import com.mrmannwood.hexlauncher.activity.BaseActivity
import com.mrmannwood.hexlauncher.appcustomize.AppCustomizationFragment
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.hexlauncher.home.HomeFragment
import com.mrmannwood.hexlauncher.isVersionStringLess
import com.mrmannwood.hexlauncher.nux.NUXHostFragment
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesDao
import com.mrmannwood.hexlauncher.settings.PreferencesRepository
import com.mrmannwood.launcher.R

class LauncherActivity : BaseActivity(), AppListFragment.AppListHostActivity {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goFullscreen()
        setContentView(R.layout.activity_launcher)
        supportActionBar?.hide()

        PreferencesRepository.getPrefs(this) { repo ->
            (application as LauncherApplication).prefsMigrationLock.await()
            if (checkShouldShowNux(repo.dao)) {
                runOnUiThread {
                    if (isDestroyed) return@runOnUiThread
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, NUXHostFragment())
                        .commit()
                }
            } else {
                runOnUiThread {
                    if (isDestroyed) return@runOnUiThread
                    if (supportFragmentManager.findFragmentById(R.id.container) == null) {
                        supportFragmentManager.beginTransaction()
                            .add(R.id.container, HomeFragment())
                            .addToBackStack("HomeFragment")
                            .commit()
                    }
                }
            }
            repo.watchPref(
                key = PreferenceKeys.Home.ORIENTATION,
                extractor = PreferenceExtractor.StringExtractor
            ).observe(this) { orientation ->
                requestedOrientation = orientation?.toIntOrNull() ?: return@observe
            }
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

    override fun getAppListHost(): AppListFragment.Host<*> {
        return appListFragmentHost
    }

    @WorkerThread
    private fun checkShouldShowNux(prefs: PreferencesDao): Boolean {
        if (isTestLab(this)) return false
        val version =
            prefs.getString(PreferenceKeys.Version.LAST_RUN_VERSION_NAME, null) ?: return true
        return isVersionStringLess(version, "1.4.1")
    }

    private fun goFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }

    private val appListFragmentHost = object : AppListFragment.Host<Void>(
        killFragment = { supportFragmentManager.popBackStack("HomeFragment", 0) }
    ) {
        override fun onSearchButtonPressed(searchTerm: String) {
            startActivity(
                Intent(Intent.ACTION_WEB_SEARCH).apply {
                    putExtra(SearchManager.QUERY, searchTerm)
                }
            )
            end()
        }

        override fun onAppSelected(appInfo: AppInfo) {
            if (!isTestLab(this@LauncherActivity)) {
                try {
                    startActivity(
                        Intent().apply { component = appInfo.componentName }
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        this@LauncherActivity,
                        R.string.unable_to_start_app,
                        Toast.LENGTH_LONG
                    ).show()
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
                        .replace(
                            R.id.container,
                            AppCustomizationFragment.forComponent(appInfo.componentName)
                        )
                        .addToBackStack("AppCustomizationFragment")
                        .commit()
                    true
                }
                menu.add(R.string.menu_item_uninstall_app_title).setOnMenuItemClickListener {
                    end()
                    startActivity(
                        Intent(Intent.ACTION_DELETE).apply {
                            data = Uri.parse("package:${appInfo.componentName.packageName}")
                        }
                    )
                    true
                }
            }
        }
    }
}
