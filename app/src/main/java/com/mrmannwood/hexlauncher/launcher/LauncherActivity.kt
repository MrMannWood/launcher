package com.mrmannwood.hexlauncher.launcher

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.hexlauncher.appupdate.AppUpdateActivityHelper
import com.mrmannwood.hexlauncher.appupdate.AppUpdateService
import com.mrmannwood.hexlauncher.contacts.ContactData
import com.mrmannwood.hexlauncher.settings.SettingsActivity
import com.mrmannwood.launcher.R

class LauncherActivity : AppCompatActivity() {

    companion object {
        private const val APP_UPDATE_REQUEST_CODE = Short.MAX_VALUE - 100
    }

    private val appUpdateActivityHelper = AppUpdateActivityHelper(
        APP_UPDATE_REQUEST_CODE,
        object : AppUpdateService.InstallListener {
            override fun onAppInstalled(completeInstall: () -> Unit) {
                AlertDialog.Builder(this@LauncherActivity)
                    .setTitle(R.string.app_update_installed_title)
                    .setMessage(R.string.app_update_installed_message)
                    .setPositiveButton(R.string.app_update_installed_positive) { _, _ ->
                        completeInstall()
                    }
                    .setNegativeButton(R.string.app_update_installed_negative) { _, _ -> }
                    .show()
            }

            override fun onInstallCancelled(retryInstall: () -> Unit) {
                AlertDialog.Builder(this@LauncherActivity)
                    .setTitle(R.string.app_update_cancelled_title)
                    .setMessage(R.string.app_update_cancelled_message)
                    .setPositiveButton(R.string.app_update_cancelled_positive) { _, _ ->
                        startActivity(Intent(this@LauncherActivity, SettingsActivity::class.java))
                    }
                    .setNegativeButton(R.string.app_update_cancelled_negative) {  _, _ -> }
                    .show()
            }

            override fun onInstallFailed(retryInstall: () -> Unit) {
                AlertDialog.Builder(this@LauncherActivity)
                    .setTitle(R.string.app_update_failed_title)
                    .setMessage(R.string.app_update_failed_message)
                    .setPositiveButton(R.string.app_update_failed_positive) { _, _ ->
                        retryInstall()
                    }
                    .setNegativeButton(R.string.app_update_failed_negative) { _, _ -> }
                    .show()
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        supportActionBar?.hide()
        makeFullScreen()

        supportFragmentManager.addFragmentOnAttachListener { _, fragment ->
            when (fragment) {
                is AppListFragment -> fragment.attachHost(appListFragmentHost)
            }
        }

        if (supportFragmentManager.findFragmentById(R.id.container) == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, HomeFragment())
                .commit()
        }

        appUpdateActivityHelper.onCreate(this)
    }

    override fun onResume() {
        super.onResume()
        appUpdateActivityHelper.onResume(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        appUpdateActivityHelper.onActivityResult(this, requestCode, resultCode, data)
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

    private fun makeFullScreen() {
        window.apply {
            getColor(R.color.black_translucent).let { color ->
                statusBarColor = color
                navigationBarColor = color
            }
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