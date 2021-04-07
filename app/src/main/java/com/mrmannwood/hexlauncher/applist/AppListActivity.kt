package com.mrmannwood.hexlauncher.applist

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.launcher.R

class AppListActivity : AppCompatActivity(), AppListFragment.AppListHostActivity {

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_PACKAGE_NAME = "package_name"
        private const val KEY_APP_NAME = "app_name"

        fun Intent.decorateForAppListLaunch(@StringRes title: Int) : Intent {
            putExtra(KEY_TITLE, title)
            return this
        }

        private fun Intent.setAppInfo(appInfo: AppInfo) {
            putExtra(KEY_PACKAGE_NAME, appInfo.packageName)
            putExtra(KEY_APP_NAME, appInfo.label)
        }

        fun Intent?.onAppListResult(
            onSuccess: (appName: String, packageName: String) -> Unit, onFailure: () -> Unit) {
            val appName = this?.getStringExtra(KEY_APP_NAME)
            val packageName = this?.getStringExtra(KEY_PACKAGE_NAME)
            if (appName == null || packageName == null) {
                onFailure()
            } else {
                onSuccess(appName, packageName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)

        val titleRes = intent.getIntExtra(KEY_TITLE, -1)
        if (titleRes != -1) {
            try {
                supportActionBar?.title = getString(titleRes)
            } catch (e: Exception) {
                finish()
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, AppListFragment())
            .commit()
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

    private val appListFragmentHost = object : AppListFragment.Host<AppInfo>(
        killFragment = { appInfo ->
            appInfo?.let {
                setResult(
                    android.app.Activity.RESULT_OK,
                    Intent().apply {
                        setAppInfo(appInfo)
                    }
                )
            } ?: run {
                setResult(android.app.Activity.RESULT_CANCELED)
            }
            finish()
        }
    ) {
        override fun onAppSelected(appInfo: AppInfo) {
            end(appInfo)
        }

        override fun showContacts(): Boolean = false
    }
}