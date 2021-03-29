package com.mrmannwood.hexlauncher.launcher

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.launcher.R

class LauncherActivity : AppCompatActivity() {

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