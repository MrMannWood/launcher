package com.mrmannwood.hexlauncher.applist

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.launcher.R
import java.lang.IllegalStateException

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

    private val hostViewModel : AppListHostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)

        hostViewModel.apply {
            supportsAppMenu.value = false
            supportsContactSearch.value = false
            contactSelected.observe(this@AppListActivity) {
                throw UnsupportedOperationException("Cannot search contacts from AppListActivity")
            }
            appSelected.observe(this@AppListActivity) { appInfo ->
                endRequested.value = appInfo
            }
            searchButtonSelected.observe(this@AppListActivity) { /* no-op */ }
            endRequested.observe(this@AppListActivity) {
                when (it) {
                    is AppInfo ->{
                        setResult(
                                android.app.Activity.RESULT_OK,
                                Intent().apply { setAppInfo(it) }
                        )
                    }
                    else -> {
                        setResult(android.app.Activity.RESULT_CANCELED)
                    }
                }
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
}