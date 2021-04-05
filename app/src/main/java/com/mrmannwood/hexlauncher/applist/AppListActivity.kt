package com.mrmannwood.hexlauncher.applist

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.launcher.R

class AppListActivity : AppCompatActivity() {

    companion object {
        private const val KEY_PACKAGE_NAME = "package_name"
        private const val KEY_APP_NAME = "app_name"

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
        supportFragmentManager.addFragmentOnAttachListener { _, fragment ->
            when (fragment) {
                is AppListFragment -> fragment.attachHost(appListFragmentHost)
                else -> throw IllegalStateException("This activity can only host AppListFragment")
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