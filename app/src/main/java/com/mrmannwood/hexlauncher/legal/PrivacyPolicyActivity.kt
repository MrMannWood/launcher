package com.mrmannwood.hexlauncher.legal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrmannwood.launcher.R

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        supportActionBar?.hide()
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, PrivacyPolicyFragment())
                .commit()
    }
}