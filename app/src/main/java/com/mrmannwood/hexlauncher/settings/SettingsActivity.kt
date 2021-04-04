package com.mrmannwood.hexlauncher.settings

import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import com.mrmannwood.launcher.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.apply {
            title = Html.fromHtml("<font color='#000000'>${getString(R.string.settings_title)}</font>", Html.FROM_HTML_MODE_LEGACY)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_root, SettingsFragment())
            .commit()
    }
}