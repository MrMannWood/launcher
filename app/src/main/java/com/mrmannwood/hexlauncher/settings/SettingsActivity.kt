package com.mrmannwood.hexlauncher.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrmannwood.launcher.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_root, SettingsFragment())
            .commit()
    }
}