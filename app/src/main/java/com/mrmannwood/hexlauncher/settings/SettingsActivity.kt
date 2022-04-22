package com.mrmannwood.hexlauncher.settings

import android.os.Bundle
import com.mrmannwood.hexlauncher.activity.BaseActivity
import com.mrmannwood.launcher.R

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.toolbar))

        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_root, SettingsFragment())
            .commit()
    }
}
