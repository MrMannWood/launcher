package com.mrmannwood.hexlauncher.launcher

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.applist.AppListHostViewModel
import com.mrmannwood.launcher.R

class LauncherActivity : AppCompatActivity() {

    private val appListHostViewModel : AppListHostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        supportActionBar?.hide()
        makeFullScreen()

        appListHostViewModel.apply {
            supportsAppMenu.value = true
            supportsContactSearch.value = true
            contactSelected.observe(this@LauncherActivity) { contact ->
                startActivity(Intent(Intent.ACTION_VIEW, contact.uri))
                endRequested.value = true
            }
            appSelected.observe(this@LauncherActivity) { appInfo ->
                packageManager.getLaunchIntentForPackage(appInfo.packageName)?.let { intent ->
                    startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    endRequested.value = true
                } ?: run {
                    Toast.makeText(this@LauncherActivity, R.string.unable_to_start_app, Toast.LENGTH_LONG).show()
                }
            }
            searchButtonSelected.observe(this@LauncherActivity) { searchTerm ->
                startActivity(Intent(Intent.ACTION_WEB_SEARCH).apply {
                    putExtra(SearchManager.QUERY, searchTerm)
                })
                endRequested.value = true
            }
            endRequested.observe(this@LauncherActivity) {
                supportFragmentManager.popBackStack()
            }
        }

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

    private fun makeFullScreen() {
        window.apply {
            getColor(R.color.black_translucent).let { color ->
                statusBarColor = color
                navigationBarColor = color
            }
        }
    }
}